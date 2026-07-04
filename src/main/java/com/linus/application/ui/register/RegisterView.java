package com.linus.application.ui.register;

import com.linus.application.backend.friend.FriendService;
import com.linus.application.backend.user.infrastructure.UserRepository;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@AnonymousAllowed
@Route(value = "register", autoLayout = false)
public class RegisterView extends Div implements LocaleChangeObserver, HasDynamicTitle {

    private final RegisterOverlay overlay;

    public RegisterView(UserRepository userRepository, PasswordEncoder passwordEncoder, FriendService friendService, UserDetailsService userDetailsService) {
        overlay = new RegisterOverlay(userRepository, passwordEncoder, userDetailsService, friendService);
        add(overlay);
        overlay.open();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        overlay.updateTexts();
    }

    @Override
    public String getPageTitle() {
        return getTranslation("register.pageTitle");
    }
}
