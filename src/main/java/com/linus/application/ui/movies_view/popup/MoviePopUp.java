package com.linus.application.ui.movies_view.popup;

import com.linus.application.backend.essay.infrastructure.Essay;
import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.movie.infrastructure.MovieService;
import com.linus.application.backend.rating.infrastructure.Rating;
import com.linus.application.backend.rating.infrastructure.RatingService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MoviePopUp extends Dialog {

    private final Movie movie;
    private final RatingService ratingService;
    private final MovieService movieService;
    private final VerticalLayout contentLayout = new VerticalLayout();

    public MoviePopUp(Movie movie, RatingService ratingService, MovieService movieService) {
        this.movie = movie;
        this.ratingService = ratingService;
        this.movieService = movieService;

        String typeLabel = movie.getType() != null ? movie.getType().getDisplayName() : "";
        setHeaderTitle(movie.getTitle() + (typeLabel.isEmpty() ? "" : " (" + typeLabel + ")"));
        setWidth("650px");

        contentLayout.setPadding(false);
        add(contentLayout);

        if (movie.getImdbId() != null && !movie.getImdbId().isBlank()) {
            String posterUrl = movieService.fetchPosterUrl(movie.getImdbId());
            if (posterUrl != null) {
                Image poster = new Image(posterUrl, movie.getTitle() + " Poster");
                poster.setMaxWidth("100%");
                poster.setMaxHeight("400px");
                contentLayout.add(poster);
            }
        }

        refreshGrid();

        Button deleteButton = new Button("Bewertung löschen", e -> {
            ratingService.deleteRatingIfRoleAllowed(grid.getSelectedItems());
            refreshGrid();
            Notification.show("Bewertung gelöscht");
        });

        Button closeButton = new Button("Schließen", e -> close());
        getFooter().add(deleteButton, closeButton);
    }

    private Grid<Rating> grid;

    private void refreshGrid() {
        contentLayout.getChildren()
                .filter(child -> child instanceof Grid || child instanceof Paragraph)
                .forEach(contentLayout::remove);

        List<Rating> ratings = ratingService.getRatingsByMovie(movie);
        List<Essay> essays = ratingService.getEssaysByRatings(ratings);
        Map<Long, String> essayMap = essays.stream()
                .collect(Collectors.toMap(e -> e.getRating().getId(), Essay::getText));

        if (ratings.isEmpty()) {
            Paragraph noRatings = new Paragraph("Keine Bewertungen vorhanden.");
            contentLayout.add(noRatings);
            grid = null;
        } else {
            grid = new Grid<>(Rating.class, false);
            grid.setItems(ratings);
            grid.addColumn(r -> r.getCategory().getCategory()).setHeader("Kategorie").setWidth("150px");
            grid.addColumn(Rating::getStars).setHeader("Sterne").setWidth("100px");
            grid.addColumn(r -> essayMap.getOrDefault(r.getId(), "-")).setHeader("Essay").setWidth("350px");
            grid.setAllRowsVisible(true);
            contentLayout.add(grid);
        }
    }
}
