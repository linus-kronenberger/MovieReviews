package com.linus.application.ui.movies_view;

import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.movie.infrastructure.MovieService;
import com.linus.application.backend.rating.infrastructure.RatingService;
import com.linus.application.ui.MainLayout;
import com.linus.application.ui.movies_view.popup.MoviePopUp;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class MovieView extends VerticalLayout {

    private final MovieService movieService;
    private final RatingService ratingService;

    private final Grid<Movie> movieGrid = new Grid<>();

    public MovieView(MovieService movieService, RatingService ratingService) {
        this.movieService = movieService;
        this.ratingService = ratingService;
        H1 h1 = new H1("Filme");

        TextField filterField = new TextField();
        filterField.setPlaceholder("Filter");
        filterField.setValueChangeMode(ValueChangeMode.LAZY);

        movieGrid.addColumn(Movie::getTitle)
            .setHeader("Titel")
            .setSortable(true)
            .setSortProperty("title");
        movieGrid.addColumn(Movie::getDescription).setHeader("Beschreibung")
                .setSortProperty("description");
        movieGrid.addColumn(m -> m.getType() != null ? m.getType().getDisplayName() : "")
                .setHeader("Typ")
                .setWidth("100px");

        movieGrid.setItems(query -> movieService
                .getAllMovies(
                        query.getOffset(),
                        query.getLimit(),
                        query.getSortOrders(),
                        filterField.getValue())
                .stream());

        movieGrid.addItemClickListener(event -> {
            Movie movie = event.getItem();
            if (movie != null) {
                MoviePopUp popup = new MoviePopUp(movie, ratingService, movieService);
                popup.open();
            }
        });

        filterField.addValueChangeListener(event ->
                movieGrid.getDataProvider().refreshAll());

        add(h1, filterField, movieGrid);
    }
}
