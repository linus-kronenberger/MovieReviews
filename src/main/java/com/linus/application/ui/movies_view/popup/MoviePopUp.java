package com.linus.application.ui.movies_view.popup;

import com.linus.application.backend.essay.infrastructure.Essay;
import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.rating.infrastructure.Rating;
import com.linus.application.backend.rating.infrastructure.RatingService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MoviePopUp extends Dialog {

    private final Movie movie;
    private final RatingService ratingService;
    private final VerticalLayout contentLayout = new VerticalLayout();

    public MoviePopUp(Movie movie, RatingService ratingService) {
        this.movie = movie;
        this.ratingService = ratingService;

        setHeaderTitle("Bewertungen - " + movie.getTitle());
        setWidth("600px");

        contentLayout.setPadding(false);
        add(contentLayout);

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
        contentLayout.removeAll();

        List<Rating> ratings = ratingService.getRatingsByMovie(movie);
        List<Essay> essays = ratingService.getEssaysByRatings(ratings);
        Map<Long, String> essayMap = essays.stream()
                .collect(Collectors.toMap(e -> e.getRating().getId(), Essay::getText));

        if (ratings.isEmpty()) {
            contentLayout.add(new Paragraph("Keine Bewertungen vorhanden."));
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
