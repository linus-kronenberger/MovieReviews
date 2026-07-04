package com.linus.application.ui.moviesview;

import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.movie.infrastructure.MovieService;
import com.linus.application.backend.review.infrastructure.ReviewService;
import com.linus.application.ui.MainLayout;
import com.linus.application.ui.moviesview.popup.MoviePopUp;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class MovieView extends VerticalLayout implements LocaleChangeObserver {

    private final MovieService movieService;
    private final ReviewService reviewService;

    private final H1 h1;
    private final TextField filterField;
    private final Grid<Movie> movieGrid = new Grid<>();

    public MovieView(MovieService movieService, ReviewService reviewService) {
        this.movieService = movieService;
        this.reviewService = reviewService;
        h1 = new H1();

        filterField = new TextField();
        filterField.setValueChangeMode(ValueChangeMode.LAZY);

        movieGrid.addColumn(Movie::getTitle)
            .setSortable(true)
            .setSortProperty("title");
        movieGrid.addColumn(Movie::getDescription)
                .setSortProperty("description");

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
                MoviePopUp popup = new MoviePopUp(movie, reviewService, movieService);
                popup.open();
            }
        });

        filterField.addValueChangeListener(event ->
                movieGrid.getDataProvider().refreshAll());

        add(h1, filterField, movieGrid);
        updateTexts();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
    }

    private void updateTexts() {
        h1.setText(getTranslation("movies.title"));
        filterField.setPlaceholder(getTranslation("movies.filter"));
        movieGrid.getColumns().get(0).setHeader(getTranslation("movies.grid.title"));
        movieGrid.getColumns().get(1).setHeader(getTranslation("movies.grid.description"));
    }
}
