package com.linus.application.ui.addreviewview;

import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.movie.infrastructure.MovieService;
import com.linus.application.backend.rating.RatingCategory;
import com.linus.application.backend.review.infrastructure.Review;
import com.linus.application.backend.review.infrastructure.ReviewService;
import com.linus.application.backend.user.infrastructure.Role;
import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.backend.user.infrastructure.UserRepository;
import com.linus.application.security.AuthenticatedUser;
import com.linus.application.ui.util.components.dialogs.ReviewOverrideDialog;
import com.linus.application.ui.util.components.formulars.StarSlider;
import com.linus.application.ui.util.components.notifications.SuccessNotification;
import com.linus.application.ui.util.components.notifications.WarningNotification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.slider.Slider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.linus.application.ui.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Route(value = "add-rating", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class AddReviewView extends VerticalLayout implements LocaleChangeObserver {

    private final MovieService movieService;
    private final ReviewService reviewService;
    private final AuthenticatedUser authenticatedUser;
    private final H1 mainHeader;
    private final H2 essayHeader;
    private final H2 ratingHeader;
    private final ComboBox<Movie> movieCombo = new ComboBox<>();
    private final List<StarSlider> starSliders = new ArrayList<>();
    private final Map<StarSlider, RatingCategory> sliderToCategory = new HashMap<>();
    private final Slider starsSlider = new Slider();
    private final TextArea essayArea = new TextArea();
    private final Button saveButton;
    private final Map<RatingCategory, Double> ratings;
    private boolean isLoading = false;
    private boolean overrideConfirmed = false;

    public AddReviewView(MovieService movieService, ReviewService reviewService,
                         AuthenticatedUser authenticatedUser, UserRepository userRepository) {

        this.movieService = movieService;
        this.reviewService = reviewService;
        this.authenticatedUser = authenticatedUser;

        mainHeader = new H1();
        add(mainHeader);

        movieCombo.setItems(movieService.getAllMoviesList());
        movieCombo.setItemLabelGenerator(Movie::getTitle);
        movieCombo.setWidthFull();
        movieCombo.addValueChangeListener(event -> onMovieSelected(event.getValue()));
        add(movieCombo);

        ratingHeader = new H2();
        add(ratingHeader);

        VerticalLayout adminRatingsLayout = new VerticalLayout();

        List<RatingCategory> adminCategories = reviewService.getAdminCategories();
        ratings = new HashMap<>();
        adminCategories.forEach(adminCategory -> {
            HorizontalLayout categorySliderLayout = new HorizontalLayout();
            StarSlider slider = new StarSlider(adminCategory.getCategory());
            slider.setValue(2.5);
            categorySliderLayout.add(slider);
            starSliders.add(slider);
            sliderToCategory.put(slider, adminCategory);
            adminRatingsLayout.add(categorySliderLayout);
            ratings.put(adminCategory, 2.5);

            slider.addValueChangeListener(event -> {
                if (!isLoading) {
                    ratings.put(adminCategory, slider.getValue());
                    autoSaveDraft();
                }
            });
        });

        add(adminRatingsLayout);

        essayArea.setWidthFull();
        essayArea.setValueChangeMode(ValueChangeMode.TIMEOUT);
        essayArea.setValueChangeTimeout(500);
        essayArea.addValueChangeListener(event -> {
            if (!isLoading) {
                autoSaveDraft();
            }
        });

        saveButton = new Button();
        saveButton.addClickListener(event -> save());

        essayHeader = new H2();
        Button customCategoriesButton = new Button(getTranslation("addrating.customCategoriesButton"));
        customCategoriesButton.addClickListener(event -> {
            Dialog customCategoriesDialog = new Dialog();
            VerticalLayout customCategoriesLayout = new VerticalLayout();
            User authUser = authenticatedUser.get().orElse(null);
            if(authUser != null && authUser.getRole() != Role.ADMIN) {
                List<RatingCategory> categories = reviewService.getCustomCategoriesForFriendsAndSelf();
                for(RatingCategory category : categories) {
                    StarSlider categorySlider = new StarSlider(category.getCategory());
                    customCategoriesLayout.add(categorySlider);
                }
                customCategoriesDialog.add(customCategoriesLayout);
                if(categories.size() > 0) {
                    customCategoriesDialog.open();
                }
            }
        });

        add(customCategoriesButton, essayHeader, essayArea);
        add(saveButton);

        updateTexts();
    }

    private void loadReviewData(Review review) {
        Map<RatingCategory, Double> savedRatings = reviewService.getDraftRatings(review);
        for (Map.Entry<RatingCategory, Double> entry : savedRatings.entrySet()) {
            ratings.put(entry.getKey(), entry.getValue());
        }
        for (StarSlider slider : starSliders) {
            RatingCategory cat = sliderToCategory.get(slider);
            Double val = savedRatings.get(cat);
            slider.setValue(val != null ? val : 2.5);
        }
        String essayText = reviewService.getDraftEssay(review);
        essayArea.setValue(essayText != null ? essayText : "");
    }

    private void resetForm() {
        for (StarSlider slider : starSliders) {
            RatingCategory cat = sliderToCategory.get(slider);
            ratings.put(cat, 2.5);
            slider.setValue(2.5);
        }
        essayArea.clear();
    }

    private void onMovieSelected(Movie movie) {
        if (movie == null) return;

        isLoading = true;
        overrideConfirmed = false;

        Optional<Review> anyReview = reviewService.findAnyReviewByMovie(movie);
        if (anyReview.isPresent() && !anyReview.get().isDraft()) {
            ReviewOverrideDialog dialog = new ReviewOverrideDialog(
                    () -> {
                        overrideConfirmed = true;
                        loadReviewData(anyReview.get());
                    },
                    this::resetForm
            );
            dialog.open();
        } else if (anyReview.isPresent()) {
            overrideConfirmed = true;
            loadReviewData(anyReview.get());
        } else {
            overrideConfirmed = true;
            resetForm();
        }

        isLoading = false;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
    }

    private void updateTexts() {
        mainHeader.setText(getTranslation("addrating.title"));
        essayHeader.setText(getTranslation("addrating.essayHeader"));
        ratingHeader.setText(getTranslation("addrating.ratingHeader"));
        starsSlider.setLabel(getTranslation("addrating.stars"));
        saveButton.setText(getTranslation("addrating.publish"));
    }

    private void autoSaveDraft() {
        Movie movie = movieCombo.getValue();
        if (movie == null) return;

        if (!overrideConfirmed) {
            Optional<Review> existing = reviewService.findAnyReviewByMovie(movie);
            if (existing.isPresent() && !existing.get().isDraft()) {
                ReviewOverrideDialog dialog = new ReviewOverrideDialog(
                        () -> {
                            overrideConfirmed = true;
                            reviewService.saveDraft(movie, ratings, essayArea.getValue());
                        },
                        () -> {}
                );
                dialog.open();
                return;
            }
            overrideConfirmed = true;
        }

        reviewService.saveDraft(movie, ratings, essayArea.getValue());
    }

    private void save() {
        Movie movie = movieCombo.getValue();

        if (movie == null) {
            WarningNotification warningNotification = new WarningNotification(getTranslation("addrating.error.movieRequired"));
            warningNotification.open();
            return;
        }

        reviewService.saveDraft(movie, ratings, essayArea.getValue());
        reviewService.publishReview(movie);

        SuccessNotification savedNotification = new SuccessNotification(getTranslation("addrating.saved"));
        savedNotification.open();

        movieCombo.clear();
        essayArea.clear();
        for (StarSlider slider : starSliders) {
            slider.setValue(2.5);
        }
    }
}
