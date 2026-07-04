package com.linus.application.ui.addmovieview;

import com.linus.application.backend.movie.infrastructure.MovieService;
import com.linus.application.backend.movie.infrastructure.MovieType;
import com.linus.application.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "add-movie", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class AddMovieView extends VerticalLayout implements LocaleChangeObserver {

    private final MovieService movieService;
    private final H1 h1;
    private final TextField titleField = new TextField();
    private final TextField descriptionField = new TextField();
    private final TextField imdbIdField = new TextField();
    private final ComboBox<MovieType> typeCombo = new ComboBox<>();
    private final Button searchImdbButton;
    private final Button saveButton;

    public AddMovieView(MovieService movieService) {
        this.movieService = movieService;

        h1 = new H1();

        titleField.setWidthFull();
        descriptionField.setWidthFull();

        imdbIdField.setWidthFull();
        imdbIdField.setRequiredIndicatorVisible(true);

        searchImdbButton = new Button();
        searchImdbButton.addClickListener(_ -> searchImdb());

        VerticalLayout imdbRow = new VerticalLayout(imdbIdField, searchImdbButton);
        imdbRow.setWidthFull();
        imdbRow.setFlexGrow(1, imdbIdField);

        typeCombo.setItems(MovieType.values());
        typeCombo.setValue(MovieType.FILM);
        typeCombo.setWidthFull();

        saveButton = new Button();
        saveButton.addClickListener(_ -> save());

        add(h1, titleField, descriptionField, imdbRow, typeCombo, saveButton);
        updateTexts();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
    }

    private void updateTexts() {
        h1.setText(getTranslation("addmovie.title"));
        titleField.setLabel(getTranslation("addmovie.field.title"));
        descriptionField.setLabel(getTranslation("addmovie.field.description"));
        imdbIdField.setLabel(getTranslation("addmovie.field.imdbId"));
        imdbIdField.setPlaceholder(getTranslation("addmovie.imdbPlaceholder"));
        searchImdbButton.setText(getTranslation("addmovie.searchImdb"));
        typeCombo.setLabel(getTranslation("addmovie.field.type"));
        saveButton.setText(getTranslation("addmovie.save"));
    }

    private void searchImdb() {
        String title = titleField.getValue();
        if (title == null || title.isBlank()) {
            Notification.show(getTranslation("addmovie.error.titleRequired"));
            return;
        }
        String imdbId = movieService.searchImdbId(title);
        if (imdbId != null) {
            imdbIdField.setValue(imdbId);
        } else {
            Notification.show(getTranslation("addmovie.error.imdbNotFound"));
        }
    }

    private void save() {
        String title = titleField.getValue();
        if (title == null || title.isBlank()) {
            Notification.show(getTranslation("addmovie.error.titleRequired"));
            return;
        }
        String imdbId = imdbIdField.getValue();
        if (imdbId == null || imdbId.isBlank()) {
            Notification.show(getTranslation("addmovie.error.imdbIdRequired"));
            return;
        }
        movieService.saveMovie(title, descriptionField.getValue(), imdbId, typeCombo.getValue());
        Notification.show(getTranslation("addmovie.saved"));
        titleField.clear();
        descriptionField.clear();
        imdbIdField.clear();
        typeCombo.setValue(MovieType.FILM);
    }
}
