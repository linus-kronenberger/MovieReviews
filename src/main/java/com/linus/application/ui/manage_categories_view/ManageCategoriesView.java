package com.linus.application.ui.manage_categories_view;

import com.linus.application.backend.rating.infrastructure.RatingCategory;
import com.linus.application.backend.rating.infrastructure.RatingService;
import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.security.AuthenticatedUser;
import com.linus.application.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "manage-categories", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class ManageCategoriesView extends VerticalLayout {

    private final RatingService ratingService;
    private final AuthenticatedUser authenticatedUser;

    private final TextField nameField = new TextField("Name");
    private final TextField descriptionField = new TextField("Beschreibung");
    private final Grid<RatingCategory> categoryGrid = new Grid<>();

    public ManageCategoriesView(RatingService ratingService, AuthenticatedUser authenticatedUser) {
        this.ratingService = ratingService;
        this.authenticatedUser = authenticatedUser;

        H1 h1 = new H1("Kategorien Verwalten");

        nameField.setWidthFull();
        descriptionField.setWidthFull();

        Button saveButton = new Button("Kategorie erstellen", event -> createCategory());

        categoryGrid.addColumn(RatingCategory::getCategory).setHeader("Name").setWidth("200px");
        categoryGrid.addColumn(RatingCategory::getDescription).setHeader("Beschreibung").setWidth("400px");
        refreshGrid();

        add(h1, nameField, descriptionField, saveButton, categoryGrid);
    }

    private void createCategory() {
        String name = nameField.getValue();
        if (name == null || name.isBlank()) {
            Notification.show("Bitte geben Sie einen Namen ein");
            return;
        }
        ratingService.createCategory(name, descriptionField.getValue());
        Notification.show("Kategorie erstellt!");
        nameField.clear();
        descriptionField.clear();
        refreshGrid();
    }

    private void refreshGrid() {
        User currentUser = authenticatedUser.get().orElse(null);
        if (currentUser != null) {
            categoryGrid.setItems(ratingService.getCategoriesByUser(currentUser));
        }
    }
}
