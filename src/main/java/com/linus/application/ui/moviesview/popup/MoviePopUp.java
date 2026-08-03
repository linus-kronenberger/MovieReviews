package com.linus.application.ui.moviesview.popup;

import com.linus.application.backend.essay.infrastructure.Essay;
import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.movie.infrastructure.MovieService;
import com.linus.application.backend.rating.RatingCategory;
import com.linus.application.backend.review.infrastructure.Review;
import com.linus.application.backend.review.infrastructure.ReviewService;
import com.linus.application.backend.user.infrastructure.Role;
import com.linus.application.security.AuthenticatedUser;
import com.linus.application.ui.util.components.dialogs.ReviewOverrideDialog;
import com.linus.application.ui.util.components.formulars.StarSlider;
import com.linus.application.ui.util.components.notifications.SuccessNotification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MoviePopUp extends Dialog implements LocaleChangeObserver {

    private final Movie movie;
    private final ReviewService reviewService;
    private final MovieService movieService;
    private final AuthenticatedUser authenticatedUser;
    private final VerticalLayout contentLayout = new VerticalLayout();
    private Button deleteButton;
    private Button closeButton;
    private Button deleteMovieButton;
    private Grid<Review> ratingGrid;
    private final Runnable onMovieDeleted;
    private final List<StarSlider> starSliders = new ArrayList<>();
    private final Map<StarSlider, RatingCategory> sliderToCategory = new HashMap<>();
    private final Map<RatingCategory, Double> ratings = new HashMap<>();
    private final TextArea essayArea = new TextArea();
    private boolean isLoading = false;
    private boolean overrideConfirmed = false;
    private H2 ratingHeader;
    private H2 essayHeader;
    private Button publishButton;

    public MoviePopUp(Movie movie, ReviewService reviewService, MovieService movieService,
                      AuthenticatedUser authenticatedUser, Runnable onMovieDeleted) {
        this.movie = movie;
        this.reviewService = reviewService;
        this.movieService = movieService;
        this.authenticatedUser = authenticatedUser;
        this.onMovieDeleted = onMovieDeleted;

        setWidth("750px");

        contentLayout.setPadding(false);
        add(contentLayout);

        HorizontalLayout posterLayout = new HorizontalLayout();
        posterLayout.setWidthFull();
        posterLayout.setAlignItems(FlexComponent.Alignment.STRETCH);

        if (movie.getImdbId() != null && !movie.getImdbId().isBlank()) {
            String posterUrl = movieService.fetchPosterUrl(movie.getImdbId());
            if (posterUrl != null) {
                Image poster = new Image(posterUrl, getTranslation("movies.popup.poster", movie.getTitle()));
                poster.setMaxWidth("200px");
                poster.setMaxHeight("300px");
                posterLayout.add(poster);
            }
        }

        TextArea descriptionArea = new TextArea();
        descriptionArea.setValue(movie.getDescription() != null ? movie.getDescription() : "");
        descriptionArea.setReadOnly(true);
        descriptionArea.setHeight("300px");
        descriptionArea.setWidthFull();
        posterLayout.add(descriptionArea);
        posterLayout.setFlexGrow(1, descriptionArea);

        contentLayout.add(posterLayout);

        ratingHeader = new H2();
        contentLayout.add(ratingHeader);

        VerticalLayout adminRatingsLayout = new VerticalLayout();
        List<RatingCategory> adminCategories = reviewService.getAdminCategories();
        adminCategories.forEach(adminCategory -> {
            StarSlider slider = new StarSlider(adminCategory.getCategory());
            slider.setValue(2.5);
            slider.addValueChangeListener(event -> {
                if (!isLoading) {
                    ratings.put(adminCategory, slider.getValue());
                    autoSaveDraft();
                }
            });
            starSliders.add(slider);
            sliderToCategory.put(slider, adminCategory);
            adminRatingsLayout.add(slider);
            ratings.put(adminCategory, 2.5);
        });
        contentLayout.add(adminRatingsLayout);

        essayHeader = new H2();
        contentLayout.add(essayHeader);

        essayArea.setWidthFull();
        essayArea.setValueChangeMode(ValueChangeMode.TIMEOUT);
        essayArea.setValueChangeTimeout(500);
        essayArea.addValueChangeListener(event -> {
            if (!isLoading) {
                autoSaveDraft();
            }
        });
        contentLayout.add(essayArea);

        publishButton = new Button();
        publishButton.addClickListener(event -> publish());
        contentLayout.add(publishButton);

        loadDraft();

        refreshGrid();

        deleteButton = new Button();
        deleteButton.addClickListener(e -> {
            reviewService.deleteRatingIfRoleAllowed(ratingGrid.getSelectedItems());
            refreshGrid();
            deleteButton.setVisible(false);
            Notification.show(getTranslation("movies.popup.deleted"));
        });
        deleteButton.setVisible(false);

        closeButton = new Button();
        closeButton.addClickListener(e -> close());

        boolean isAdmin = authenticatedUser.get()
                .map(u -> u.getRole() == Role.ADMIN)
                .orElse(false);

        if (isAdmin) {
            deleteMovieButton = new Button();
            deleteMovieButton.addClickListener(e -> {
                movieService.deleteMovie(movie);
                Notification.show(getTranslation("movies.popup.movieDeleted"));
                close();
                onMovieDeleted.run();
            });
        }

        getFooter().add(deleteButton, closeButton);
        if (deleteMovieButton != null) {
            getFooter().add(deleteMovieButton);
        }
        updateTexts();
    }

    private void autoSaveDraft() {
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

    private void publish() {
        autoSaveDraft();
        reviewService.publishReview(movie);
        overrideConfirmed = false;
        SuccessNotification notification = new SuccessNotification(getTranslation("movies.popup.published"));
        notification.open();
        refreshGrid();
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

    private void loadDraft() {
        isLoading = true;
        overrideConfirmed = false;

        Optional<Review> anyReview = reviewService.findAnyReviewByMovie(movie);
        if (anyReview.isPresent() && !anyReview.get().isDraft()) {
            ReviewOverrideDialog dialog = new ReviewOverrideDialog(
                    () -> {
                        overrideConfirmed = true;
                        loadReviewData(anyReview.get());
                    },
                    () -> {}
            );
            dialog.open();
        } else if (anyReview.isPresent()) {
            overrideConfirmed = true;
            loadReviewData(anyReview.get());
        } else {
            overrideConfirmed = true;
        }

        isLoading = false;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
        refreshGrid();
    }

    private void updateTexts() {
        String typeLabel = movie.getType() != null ? movie.getType().getDisplayName() : "";
        setHeaderTitle(movie.getTitle() + (typeLabel.isEmpty() ? "" : " (" + typeLabel + ")"));
        if (ratingHeader != null) {
            ratingHeader.setText(getTranslation("movies.popup.ratingHeader"));
        }
        if (essayHeader != null) {
            essayHeader.setText(getTranslation("movies.popup.essayHeader"));
        }
        publishButton.setText(getTranslation("movies.popup.publish"));
        deleteButton.setText(getTranslation("movies.popup.delete"));
        closeButton.setText(getTranslation("movies.popup.close"));
        if (deleteMovieButton != null) {
            deleteMovieButton.setText(getTranslation("movies.popup.deleteMovie"));
        }
        if (ratingGrid != null) {
            ratingGrid.getColumns().get(0).setHeader(getTranslation("movies.popup.grid.user"));
            ratingGrid.getColumns().get(1).setHeader(getTranslation("movies.popup.grid.standardRating"));
            ratingGrid.getColumns().get(2).setHeader(getTranslation("movies.popup.grid.customRating"));
            ratingGrid.getColumns().get(3).setHeader(getTranslation("movies.popup.grid.avgrating"));
            ratingGrid.getColumns().get(4).setHeader(getTranslation("movies.popup.grid.essay"));
        }
    }

    private void refreshGrid() {
        contentLayout.getChildren()
                .filter(child -> child instanceof Grid || child instanceof Paragraph)
                .forEach(contentLayout::remove);

        List<Review> reviews = reviewService.getPublishedRatingsByMovie(movie);
        List<Essay> essays = reviewService.getEssaysByRatings(reviews);
        Map<String, String> essayMap = essays.stream()
                .collect(Collectors.toMap(e -> e.getRating().getId(), Essay::getText));

        Map<String, double[]> threeAverages = reviewService.getThreeAveragesForReviews(reviews);

        if (reviews.isEmpty()) {
            Paragraph noRatings = new Paragraph(getTranslation("movies.popup.noRatings"));
            contentLayout.add(noRatings);
            ratingGrid = null;
        } else {
            ratingGrid = new Grid<>(Review.class, false);
            ratingGrid.addItemClickListener(_ -> onRatingClicked());
            ratingGrid.setItems(reviews);
            ratingGrid.addColumn(r -> r.getUser().getUsername()).setHeader(getTranslation("movies.popup.grid.user")).setWidth("90px");
            ratingGrid.addColumn(r -> formatAvg(threeAverages, r.getId(), 0)).setHeader(getTranslation("movies.popup.grid.standardRating")).setWidth("90px");
            ratingGrid.addColumn(r -> formatAvg(threeAverages, r.getId(), 1)).setHeader(getTranslation("movies.popup.grid.customRating")).setWidth("90px");
            ratingGrid.addColumn(r -> formatAvg(threeAverages, r.getId(), 2)).setHeader(getTranslation("movies.popup.grid.avgrating")).setWidth("90px");
            ratingGrid.addColumn(r -> essayMap.getOrDefault(r.getId(), "-")).setHeader(getTranslation("movies.popup.grid.essay")).setWidth("290px");
            ratingGrid.setAllRowsVisible(true);
            contentLayout.add(ratingGrid);
        }
    }

    private String formatAvg(Map<String, double[]> threeAverages, String reviewId, int index) {
        double[] avgs = threeAverages.get(reviewId);
        if (avgs == null || Double.isNaN(avgs[index])) return "-";
        return String.format("%.1f", avgs[index]);
    }

    private void onRatingClicked() {
        if (ratingGrid.getSelectedItems().isEmpty()) {
            deleteButton.setVisible(false);
        } else {
            deleteButton.setVisible(true);
        }
    }
}
