package com.linus.application.ui.add_rating_view;

import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.movie.infrastructure.MovieService;
import com.linus.application.backend.rating.infrastructure.RatingCategory;
import com.linus.application.backend.rating.infrastructure.RatingService;
import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.security.AuthenticatedUser;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.slider.Slider;
import com.linus.application.ui.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "add-rating", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class AddRatingView extends VerticalLayout {

    private final MovieService movieService;
    private final RatingService ratingService;
    private final AuthenticatedUser authenticatedUser;

    private final ComboBox<Movie> movieCombo = new ComboBox<>("Film");
    private final ComboBox<RatingCategory> categoryCombo = new ComboBox<>("Kategorie");
    private final Slider starsSlider = new Slider("Sterne");
    private final TextArea essayArea = new TextArea("Essay");

    public AddRatingView(MovieService movieService, RatingService ratingService,
                         AuthenticatedUser authenticatedUser) {
        this.movieService = movieService;
        this.ratingService = ratingService;
        this.authenticatedUser = authenticatedUser;

        H1 h1 = new H1("Bewertung Hinzufügen");

        movieCombo.setItems(movieService.getAllMoviesList());
        movieCombo.setItemLabelGenerator(Movie::getTitle);
        movieCombo.setWidthFull();

        User currentUser = authenticatedUser.get().orElse(null);
        if (currentUser != null) {
            categoryCombo.setItems(ratingService.getCategoriesByUser(currentUser));
        }
        categoryCombo.setItemLabelGenerator(RatingCategory::getCategory);
        categoryCombo.setWidthFull();

        starsSlider.setMin(1);
        starsSlider.setMax(10);
        starsSlider.setStep(1);
        starsSlider.setValue(5.0);
        starsSlider.setMinMaxVisible(true);
        starsSlider.setValueAlwaysVisible(true);
        starsSlider.setWidthFull();

        essayArea.setWidthFull();

        Button saveButton = new Button("Speichern", event -> save());

        add(h1, movieCombo, categoryCombo, starsSlider, essayArea, saveButton);
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

        int stars = (int) Math.round(starsSlider.getValue());

        ratingService.saveRating(movie, category, stars, essayArea.getValue());
        Notification.show("Bewertung gespeichert!");

        movieCombo.clear();
        categoryCombo.clear();
        starsSlider.setValue(5.0);
        essayArea.clear();
    }
}
