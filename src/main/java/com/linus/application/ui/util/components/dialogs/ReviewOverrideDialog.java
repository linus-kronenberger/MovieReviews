package com.linus.application.ui.util.components.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

public class ReviewOverrideDialog extends ConfirmDialog {
  public ReviewOverrideDialog() {
    this.setText(getTranslation("addrating.reviewOverrideDialog"));
    this.setCancelable(true);
    Button confirmButton = new Button(getTranslation("confirm"));
    confirmButton.addThemeVariants(ButtonVariant.SUCCESS);
    Button cancelButton = new Button(getTranslation("cancel"));
    cancelButton.addThemeVariants(ButtonVariant.ERROR);
    this.setConfirmButton(confirmButton);
    this.setCancelButton(cancelButton);
  }
}
