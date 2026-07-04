package com.linus.application.ui.register;

import com.linus.application.backend.user.infrastructure.Role;
import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.backend.user.infrastructure.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.security.crypto.password.PasswordEncoder;

public class RegisterOverlay extends Dialog {

    private final H2 title;
    private final Paragraph description;
    private final TextField username;
    private final PasswordField password;
    private final PasswordField confirmPassword;
    private final Button register;
    private final Button cancel;

    public RegisterOverlay(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        setDraggable(false);
        setCloseOnOutsideClick(false);
        setWidth("400px");

        VerticalLayout card = new VerticalLayout();
        card.setPadding(false);
        card.getStyle().set("padding", "var(--lumo-space-xl)");

        title = new H2();
        title.getStyle().set("margin-top", "0");

        description = new Paragraph();
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        username = new TextField();
        username.setWidthFull();
        password = new PasswordField();
        password.setWidthFull();
        confirmPassword = new PasswordField();
        confirmPassword.setWidthFull();

        register = new Button();
        register.setWidthFull();
        register.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        cancel = new Button();
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancel.setWidthFull();

        register.addClickListener(e -> {
            String user = username.getValue();
            if (user == null || user.isBlank()) {
                Notification.show(getTranslation("register.error.usernameRequired"));
                return;
            }
            if (userRepository.existsByUsername(user)) {
                Notification.show(getTranslation("register.error.usernameExists"));
                return;
            }
            if (!password.getValue().equals(confirmPassword.getValue())) {
                Notification.show(getTranslation("register.error.passwordMismatch"));
                return;
            }
            User newUser = new User();
            newUser.setUsername(user);
            newUser.setName(user);
            newUser.setPasswordHash(passwordEncoder.encode(password.getValue()));
            newUser.setRole(Role.USER);
            userRepository.save(newUser);
            Notification.show(getTranslation("register.success"));
            close();
            getUI().ifPresent(ui -> ui.navigate("login"));
        });

        cancel.addClickListener(e -> {
            close();
            getUI().ifPresent(ui -> ui.navigate("login"));
        });

        card.add(title, description, username, password, confirmPassword, register, cancel);
        card.setAlignItems(FlexComponent.Alignment.STRETCH);
        add(card);
        updateTexts();
    }

    public void updateTexts() {
        title.setText(getTranslation("register.title"));
        description.setText(getTranslation("register.description"));
        username.setLabel(getTranslation("register.field.username"));
        password.setLabel(getTranslation("register.field.password"));
        confirmPassword.setLabel(getTranslation("register.field.confirmPassword"));
        register.setText(getTranslation("register.button"));
        cancel.setText(getTranslation("register.toLogin"));
    }
}
