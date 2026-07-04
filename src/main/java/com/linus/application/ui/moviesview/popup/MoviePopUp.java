package com.linus.application.ui.moviesview.popup;

import com.linus.application.backend.essay.infrastructure.Essay;
import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.movie.infrastructure.MovieService;
import com.linus.application.backend.review.infrastructure.Review;
import com.linus.application.backend.review.infrastructure.ReviewService;
import com.linus.application.backend.user.infrastructure.Role;
import com.linus.application.security.AuthenticatedUser;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MoviePopUp extends Dialog implements LocaleChangeObserver {

    private final Movie movie;
    private final ReviewService reviewService;
    private final MovieService movieService;
    private final VerticalLayout contentLayout = new VerticalLayout();
    private Button deleteButton;
    private Button closeButton;
    private Button deleteMovieButton;
    private Grid<Review> ratingGrid;
    private final Runnable onMovieDeleted;

    public MoviePopUp(Movie movie, ReviewService reviewService, MovieService movieService,
                      AuthenticatedUser authenticatedUser, Runnable onMovieDeleted) {
        this.movie = movie;
        this.reviewService = reviewService;
        this.movieService = movieService;
        this.onMovieDeleted = onMovieDeleted;

        setWidth("650px");

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

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
        refreshGrid();
    }

    private void updateTexts() {
        String typeLabel = movie.getType() != null ? movie.getType().getDisplayName() : "";
        setHeaderTitle(movie.getTitle() + (typeLabel.isEmpty() ? "" : " (" + typeLabel + ")"));
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
            ratingGrid.addItemClickListener(_->onRatingClicked());
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
        if(ratingGrid.getSelectedItems().isEmpty()) {
            deleteButton.setVisible(false);
        } else {
            deleteButton.setVisible(true);
        }

    }
}
