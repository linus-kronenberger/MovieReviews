package com.linus.application.ui.register;

import com.linus.application.backend.user.infrastructure.Role;
import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.backend.user.infrastructure.UserRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;

@AnonymousAllowed
@PageTitle("Registrieren")
@Route(value = "register")
public class RegisterView extends VerticalLayout {

    private final TextField username;
    private final TextField name;
    private final PasswordField password;
    private final PasswordField confirmPassword;
    private final Button register;
    private final Button cancel;

    private final Binder<User> binder;

    public RegisterView(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        H2 title = new H2("Registrieren");

        username = new TextField("Benutzername");
        name = new TextField("Anzeigename");
        password = new PasswordField("Passwort");
        confirmPassword = new PasswordField("Passwort bestätigen");
        register = new Button("Registrieren");
        cancel = new Button("Abbrechen");

        binder = new Binder<>(User.class);
        binder.forField(username)
                .asRequired("Benutzername ist erforderlich")
                .withValidator(u -> !userRepository.existsByUsername(u), "Benutzername existiert bereits")
                .bind(User::getUsername, User::setUsername);
        binder.forField(name)
                .asRequired("Anzeigename ist erforderlich")
                .bind(User::getName, User::setName);
        binder.forField(password)
                .asRequired("Passwort ist erforderlich")
                .withValidator(p -> p.length() >= 6, "Passwort muss mindestens 6 Zeichen lang sein")
                .bind(User::getPasswordHash, User::setPasswordHash);

        register.addClickListener(e -> {
            if (!password.getValue().equals(confirmPassword.getValue())) {
                Notification.show("Passwörter stimmen nicht überein");
                return;
            }
            try {
                User user = new User();
                binder.writeBean(user);
                user.setPasswordHash(passwordEncoder.encode(password.getValue()));
                user.setRole(Role.USER);
                userRepository.save(user);
                Notification.show("Registrierung erfolgreich! Du kannst dich jetzt einloggen.");
                getUI().ifPresent(ui -> ui.navigate("login"));
            } catch (ValidationException ex) {
                Notification.show("Bitte überprüfe deine Eingaben");
            }
        });

        cancel.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("login")));

        FormLayout form = new FormLayout();
        form.add(username, name, password, confirmPassword);

        setAlignItems(Alignment.CENTER);
        add(title, form, register, cancel);
        setMaxWidth("400px");
    }
}
