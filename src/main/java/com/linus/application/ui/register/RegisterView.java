package com.linus.application.ui.register;

import com.linus.application.backend.user.infrastructure.UserRepository;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;

@AnonymousAllowed
@PageTitle("Registrieren")
@Route(value = "register", autoLayout = false)
public class RegisterView extends Div {

    public RegisterView(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        RegisterOverlay overlay = new RegisterOverlay(userRepository, passwordEncoder);
        add(overlay);
        overlay.open();
    }
}
