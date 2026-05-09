package com.linus.application.ui.movies_view;

import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.movie.infrastructure.MovieService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

/**
 * This is the MovieView.
 * Paginating in Vaadin / Spring Boot:
 * https://vaadin.com/docs/latest/building-apps/forms-data/add-grid/paginated-data
 */
@Route("")
public class MovieView extends VerticalLayout {


    private final MovieService movieService;

    /**
     * This is the Main View of the App :D
     *
     */
    public MovieView(MovieService movieService) {
        this.movieService = movieService;
        // Define DOM
        H1 h1 = new H1("Movies");

        TextField filterField = new TextField();
        filterField.setPlaceholder("Filter");
        filterField.setValueChangeMode(ValueChangeMode.LAZY);

        Grid<Movie> movieGrid = new Grid<>();
        movieGrid.addColumn(Movie::getTitle).setHeader("Titel")
                .setSortProperty("movie_id");
        movieGrid.addColumn(Movie::getDescription).setHeader("Beschreibung")
                .setSortProperty("description");

        movieGrid.setItems(query-> movieService
                .getAllMovies(
                        query.getOffset(),
                        query.getLimit(),
                        query.getSortOrders(),
                        filterField.getValue())
                .stream());

        filterField.addValueChangeListener(event ->
            movieGrid.getDataProvider().refreshAll());

        // Add the components
        add(h1, filterField, movieGrid);

    }
}
