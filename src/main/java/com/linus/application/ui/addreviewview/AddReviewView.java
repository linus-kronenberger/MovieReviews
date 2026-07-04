package com.linus.application.ui.addreviewview;

import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.movie.infrastructure.MovieService;
import com.linus.application.backend.rating.RatingCategory;
import com.linus.application.backend.review.infrastructure.ReviewService;
import com.linus.application.backend.user.infrastructure.UserRepository;
import com.linus.application.security.AuthenticatedUser;
import com.linus.application.ui.util.components.dialogs.ReviewOverrideDialog;
import com.linus.application.ui.util.components.formulars.StarSlider;
import com.linus.application.ui.util.components.notifications.SuccessNotification;
import com.linus.application.ui.util.components.notifications.WarningNotification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.slider.Slider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.linus.application.ui.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final Slider starsSlider = new Slider();
    private final TextArea essayArea = new TextArea();
    private final Button saveButton;
    private final Button openCustomCategoryPopupButton;
    private final Map<RatingCategory, Double> ratings;

    public AddReviewView(MovieService movieService, ReviewService reviewService,
                         AuthenticatedUser authenticatedUser, UserRepository userRepository) {

        // ---------------------
        // dependencies
        // ---------------------

        this.movieService = movieService;
        this.reviewService = reviewService;
        this.authenticatedUser = authenticatedUser;

        mainHeader = new H1();
        add(mainHeader);

        movieCombo.setItems(movieService.getAllMoviesList());
        movieCombo.setItemLabelGenerator(Movie::getTitle);
        movieCombo.setWidthFull();
        add(movieCombo);

        ratingHeader = new H2();
        add(ratingHeader);

        VerticalLayout adminRatingsLayout = new VerticalLayout();

        // ---------------------
        // add rating sliders for admin categories
        // ---------------------

        List<RatingCategory> adminCategories = reviewService.getAdminCategories();
        ratings = new HashMap<RatingCategory, Double>();
        adminCategories.forEach(adminCategory -> {
            HorizontalLayout categorySliderLayout = new HorizontalLayout();
            StarSlider slider = new StarSlider(adminCategory.getCategory());
            slider.setValue(2.5);
            categorySliderLayout.add(slider);
            starSliders.add(slider);
            adminRatingsLayout.add(categorySliderLayout);
            ratings.put(adminCategory, 2.5);
            slider.addValueChangeListener(event -> {
               ratings.put(adminCategory, slider.getValue());
            });
        });

        add(adminRatingsLayout);

        openCustomCategoryPopupButton = new Button();
        openCustomCategoryPopupButton.addClickListener(_->{openCustomCategoryPopUp();});

        add(openCustomCategoryPopupButton);

        essayArea.setWidthFull();

        saveButton = new Button();
        saveButton.addClickListener(event -> save());

        essayHeader = new H2();
        add(essayHeader, essayArea);
        add(saveButton);
        // ---------------------
        // localization text updates
        // ---------------------
        updateTexts();
    }

    private void openCustomCategoryPopUp() {
        Dialog dialog = new Dialog();
        dialog.open();
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
        saveButton.setText(getTranslation("addrating.save"));
        openCustomCategoryPopupButton.setText(getTranslation("addrating.openCustomCategoryPopup"));
    }

    private void save() {
        Movie movie = movieCombo.getValue();

        // ---------------------
        // form check
        // ---------------------

        if (movie == null) {
            WarningNotification warningNotification = new WarningNotification(getTranslation("addrating.error.movieRequired"));
            warningNotification.open();
            return;
        }

        // ---------------------
        // warning if review already exists
        // ---------------------

        if(reviewService.reviewForUserExists()) {
            ReviewOverrideDialog reviewOverrideDialog = new ReviewOverrideDialog();
            reviewOverrideDialog.open();
            reviewOverrideDialog.addRejectListener(_ -> {
                return ;
            });
            reviewOverrideDialog.addConfirmListener(_ -> {
                reviewService.deleteReviewByUser();
                saveNotifyReview(movie);
            });
            return;
        }

        saveNotifyReview(movie);
    }

    private void saveNotifyReview(Movie movie) {
        reviewService.saveReview(movie, ratings,essayArea.getValue());

        SuccessNotification savedNotification = new SuccessNotification(getTranslation("addrating.saved"));
        savedNotification.open();

        movieCombo.clear();
        starsSlider.setValue(5.0);
        essayArea.clear();
    }
 }
