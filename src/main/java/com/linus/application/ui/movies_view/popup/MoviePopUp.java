package com.linus.application.ui.movies_view.popup;

import com.linus.application.backend.essay.infrastructure.Essay;
import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.rating.infrastructure.Rating;
import com.linus.application.backend.rating.infrastructure.RatingService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MoviePopUp extends Dialog {

    public MoviePopUp(Movie movie, RatingService ratingService) {
        List<Rating> ratings = ratingService.getRatingsByMovie(movie);
        List<Essay> essays = ratingService.getEssaysByRatings(ratings);
        Map<Long, String> essayMap = essays.stream()
                .collect(Collectors.toMap(e -> e.getRating().getId(), Essay::getText));

        setHeaderTitle("Bewertungen - " + movie.getTitle());
        setWidth("600px");

        if (ratings.isEmpty()) {
            add(new Paragraph("Keine Bewertungen vorhanden."));
        } else {
            Grid<Rating> grid = new Grid<>();
            grid.setItems(ratings);
            grid.addColumn(r -> r.getCategory().getCategory()).setHeader("Kategorie").setWidth("150px");
            grid.addColumn(Rating::getStars).setHeader("Sterne").setWidth("100px");
            grid.addColumn(r -> essayMap.getOrDefault(r.getId(), "-")).setHeader("Essay").setWidth("350px");
            grid.setAllRowsVisible(true);

            VerticalLayout layout = new VerticalLayout(grid);
            layout.setPadding(false);
            add(layout);
        }

        Button closeButton = new Button("Schlie\u00dfen", e -> close());
        getFooter().add(closeButton);
    }
}
