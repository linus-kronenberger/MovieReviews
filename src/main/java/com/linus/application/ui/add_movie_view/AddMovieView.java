package com.linus.application.ui.add_movie_view;

import com.linus.application.backend.movie.infrastructure.MovieService;
import com.linus.application.backend.movie.infrastructure.MovieType;
import com.linus.application.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "add-movie", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class AddMovieView extends VerticalLayout {

    private final MovieService movieService;

    private final TextField titleField = new TextField("Titel");
    private final TextField descriptionField = new TextField("Beschreibung");
    private final TextField imdbIdField = new TextField("IMDb ID");
    private final ComboBox<MovieType> typeCombo = new ComboBox<>("Typ");

    public AddMovieView(MovieService movieService) {
        this.movieService = movieService;

        H1 h1 = new H1("Neuen Film anlegen");

        titleField.setWidthFull();
        descriptionField.setWidthFull();
        imdbIdField.setWidthFull();
        imdbIdField.setPlaceholder("z.B. tt1375666");

        typeCombo.setItems(MovieType.values());
        typeCombo.setItemLabelGenerator(MovieType::getDisplayName);
        typeCombo.setValue(MovieType.FILM);
        typeCombo.setWidthFull();

        Button saveButton = new Button("Speichern", event -> save());

        add(h1, titleField, descriptionField, imdbIdField, typeCombo, saveButton);
    }

    private void save() {
        String title = titleField.getValue();
        if (title == null || title.isBlank()) {
            Notification.show("Bitte geben Sie einen Titel ein");
            return;
        }
        String imdbId = imdbIdField.getValue();
        if (imdbId != null && imdbId.isBlank()) {
            imdbId = null;
        }
        movieService.saveMovie(title, descriptionField.getValue(), imdbId, typeCombo.getValue());
        Notification.show("Film gespeichert!");
        titleField.clear();
        descriptionField.clear();
        imdbIdField.clear();
        typeCombo.setValue(MovieType.FILM);
    }
}
