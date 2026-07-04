package com.linus.application.ui.moviesview.popup;

import com.linus.application.backend.essay.infrastructure.Essay;
import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.movie.infrastructure.MovieService;
import com.linus.application.backend.review.infrastructure.Review;
import com.linus.application.backend.review.infrastructure.ReviewService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

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
    private Grid<Review> ratingGrid;

    public MoviePopUp(Movie movie, ReviewService reviewService, MovieService movieService) {
        this.movie = movie;
        this.reviewService = reviewService;
        this.movieService = movieService;

        setWidth("650px");

        contentLayout.setPadding(false);
        add(contentLayout);

        if (movie.getImdbId() != null && !movie.getImdbId().isBlank()) {
            String posterUrl = movieService.fetchPosterUrl(movie.getImdbId());
            if (posterUrl != null) {
                Image poster = new Image(posterUrl, getTranslation("movies.popup.poster", movie.getTitle()));
                poster.setMaxWidth("100%");
                poster.setMaxHeight("400px");
                contentLayout.add(poster);
            }
        }

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
        getFooter().add(deleteButton, closeButton);
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
        if (ratingGrid != null) {
            ratingGrid.getColumns().get(0).setHeader(getTranslation("movies.popup.grid.category"));
            ratingGrid.getColumns().get(1).setHeader(getTranslation("movies.popup.grid.avgrating"));
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

        if (reviews.isEmpty()) {
            Paragraph noRatings = new Paragraph(getTranslation("movies.popup.noRatings"));
            contentLayout.add(noRatings);
            ratingGrid = null;
        } else {
            ratingGrid = new Grid<>(Review.class, false);
            ratingGrid.addItemClickListener(_->onRatingClicked());
            ratingGrid.setItems(reviews);
            ratingGrid.addColumn(r -> r.getAverageRating()).setHeader(getTranslation("movies.popup.grid.avgrating")).setWidth("150px");
            ratingGrid.addColumn(r -> essayMap.getOrDefault(r.getId(), "-")).setHeader(getTranslation("movies.popup.grid.essay")).setWidth("350px");
            ratingGrid.setAllRowsVisible(true);
            contentLayout.add(ratingGrid);
        }
    }

    private void onRatingClicked() {
        if(ratingGrid.getSelectedItems().isEmpty()) {
            deleteButton.setVisible(false);
        } else {
            deleteButton.setVisible(true);
        }

    }
}
