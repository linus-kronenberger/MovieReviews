package com.linus.application.ui.managecategoriesview;

import com.linus.application.backend.rating.RatingCategory;
import com.linus.application.backend.review.infrastructure.ReviewService;
import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.security.AuthenticatedUser;
import com.linus.application.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "manage-categories", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class ManageCategoriesView extends VerticalLayout implements LocaleChangeObserver {

  private final ReviewService reviewService;
  private final AuthenticatedUser authenticatedUser;

  private final H1 h1;
  private final TextField nameField = new TextField();
  private final TextField descriptionField = new TextField();
  private final Grid<RatingCategory> categoryGrid = new Grid<>();
  private final Button saveButton;
  private final Button deleteButton;

  public ManageCategoriesView(ReviewService reviewService, AuthenticatedUser authenticatedUser) {
    this.reviewService = reviewService;
    this.authenticatedUser = authenticatedUser;

    h1 = new H1();

    nameField.setWidthFull();
    descriptionField.setWidthFull();

    HorizontalLayout buttonLayout = new HorizontalLayout();
    saveButton = new Button();
    saveButton.addClickListener(event -> createCategory());
    deleteButton = new Button();
    deleteButton.addClickListener(event -> deleteSelected());
    deleteButton.setVisible(false);
    buttonLayout.add(saveButton, deleteButton);
    categoryGrid.addColumn(RatingCategory::getCategory).setWidth("200px");
    categoryGrid.addColumn(RatingCategory::getDescription).setWidth("400px");
    categoryGrid.addSelectionListener(_ -> onCategorySelected());
    refreshGrid();

    add(h1, nameField, descriptionField, buttonLayout, categoryGrid);
    updateTexts();
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    updateTexts();
  }

  private void updateTexts() {
    h1.setText(getTranslation("categories.title"));
    nameField.setLabel(getTranslation("categories.field.name"));
    descriptionField.setLabel(getTranslation("categories.field.description"));
    saveButton.setText(getTranslation("categories.create"));
    deleteButton.setText(getTranslation("categories.delete"));
    categoryGrid.getColumns().get(0).setHeader(getTranslation("categories.grid.name"));
    categoryGrid.getColumns().get(1).setHeader(getTranslation("categories.grid.description"));
  }

  private void onCategorySelected() {
    if (categoryGrid.getSelectedItems().isEmpty()) {
      deleteButton.setVisible(false);
    } else {
      deleteButton.setVisible(true);
    }
  }

  private void createCategory() {
    String name = nameField.getValue();
    if (name == null || name.isBlank()) {
      Notification.show(getTranslation("categories.error.nameRequired"));
      return;
    }
    reviewService.createCategory(name, descriptionField.getValue());
    Notification.show(getTranslation("categories.created"));
    nameField.clear();
    descriptionField.clear();
    refreshGrid();
  }

  private void deleteSelected() {
    for (RatingCategory category : categoryGrid.getSelectedItems()) {
      reviewService.deleteCategory(category.getId());
    }
    Notification.show(getTranslation("categories.deleted"));
    refreshGrid();
  }

  private void refreshGrid() {
    User currentUser = authenticatedUser.get().orElse(null);
    if (currentUser != null) {
      categoryGrid.setItems(reviewService.getCategoriesByUser(currentUser));
    }
  }
}
