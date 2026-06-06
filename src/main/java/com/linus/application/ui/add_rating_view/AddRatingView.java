package com.linus.application.ui.add_rating_view;

import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.movie.infrastructure.MovieService;
import com.linus.application.backend.rating.infrastructure.RatingCategory;
import com.linus.application.backend.rating.infrastructure.RatingService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("/add-rating")
public class AddRatingView extends VerticalLayout {

    private final MovieService movieService;
    private final RatingService ratingService;

    private final ComboBox<Movie> movieCombo = new ComboBox<>("Film");
    private final ComboBox<RatingCategory> categoryCombo = new ComboBox<>("Kategorie");
    private final TextField starsField = new TextField("Sterne (1-10)");
    private final TextArea essayArea = new TextArea("Essay");

    public AddRatingView(MovieService movieService, RatingService ratingService) {
        this.movieService = movieService;
        this.ratingService = ratingService;

        H1 h1 = new H1("Bewertung Hinzufügen");

        movieCombo.setItems(movieService.getAllMoviesList());
        movieCombo.setItemLabelGenerator(Movie::getTitle);
        movieCombo.setWidthFull();

        categoryCombo.setItems(ratingService.getAllCategories());
        categoryCombo.setItemLabelGenerator(RatingCategory::getCategory);
        categoryCombo.setWidthFull();

        starsField.setWidthFull();
        starsField.setPattern("[1-9]|10");
        starsField.setAllowedCharPattern("[0-9]");
        starsField.setErrorMessage("Bitte eine Zahl zwischen 1 und 10 eingeben");

        essayArea.setWidthFull();

        Button saveButton = new Button("Speichern", event -> save());

        add(h1, movieCombo, categoryCombo, starsField, essayArea, saveButton);
    }

    private void save() {
        Movie movie = movieCombo.getValue();
        if (movie == null) {
            Notification.show("Bitte wählen Sie einen Film aus");
            return;
        }
        RatingCategory category = categoryCombo.getValue();
        if (category == null) {
            Notification.show("Bitte wählen Sie eine Kategorie aus");
            return;
        }
        String starsText = starsField.getValue();
        if (starsText == null || starsText.isBlank()) {
            Notification.show("Bitte geben Sie eine Sternebewertung ein");
            return;
        }
        Integer stars = Integer.parseInt(starsText);
        if (stars < 1 || stars > 10) {
            Notification.show("Die Sternebewertung muss zwischen 1 und 10 liegen");
            return;
        }

        ratingService.saveRating(movie, category, stars, essayArea.getValue());
        Notification.show("Bewertung gespeichert!");

        movieCombo.clear();
        categoryCombo.clear();
        starsField.clear();
        essayArea.clear();
    }
}
