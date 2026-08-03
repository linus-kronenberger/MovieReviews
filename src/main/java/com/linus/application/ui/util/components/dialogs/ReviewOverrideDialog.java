package com.linus.application.ui.util.components.dialogs;

import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

public class ReviewOverrideDialog extends ConfirmDialog {

    public ReviewOverrideDialog(Runnable onConfirm, Runnable onCancel) {
        setText(getTranslation("addrating.reviewOverrideDialog"));
        setCancelable(true);
        setConfirmButton(getTranslation("confirm"), e -> onConfirm.run());
        setCancelButton(getTranslation("cancel"), e -> onCancel.run());
    }
}
