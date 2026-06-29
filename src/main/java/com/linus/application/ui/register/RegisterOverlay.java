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

    public RegisterOverlay(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        setDraggable(false);
        setCloseOnOutsideClick(false);
        setWidth("400px");

        VerticalLayout card = new VerticalLayout();
        card.setPadding(false);
        card.getStyle().set("padding", "var(--lumo-space-xl)");

        H2 title = new H2("Registrieren");
        title.getStyle().set("margin-top", "0");

        Paragraph description = new Paragraph("Erstelle ein neues Konto");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        TextField username = new TextField("Benutzername");
        username.setWidthFull();
        PasswordField password = new PasswordField("Passwort");
        password.setWidthFull();
        PasswordField confirmPassword = new PasswordField("Passwort bestätigen");
        confirmPassword.setWidthFull();

        Button register = new Button("Registrieren");
        register.setWidthFull();
        register.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Zum Login");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancel.setWidthFull();

        register.addClickListener(e -> {
            String user = username.getValue();
            if (user == null || user.isBlank()) {
                Notification.show("Bitte geben Sie einen Benutzernamen ein");
                return;
            }
            if (userRepository.existsByUsername(user)) {
                Notification.show("Benutzername existiert bereits");
                return;
            }
            if (!password.getValue().equals(confirmPassword.getValue())) {
                Notification.show("Passwörter stimmen nicht überein");
                return;
            }
            User newUser = new User();
            newUser.setUsername(user);
            newUser.setName(user);
            newUser.setPasswordHash(passwordEncoder.encode(password.getValue()));
            newUser.setRole(Role.USER);
            userRepository.save(newUser);
            Notification.show("Registrierung erfolgreich! Du kannst dich jetzt einloggen.");
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
    }
}
