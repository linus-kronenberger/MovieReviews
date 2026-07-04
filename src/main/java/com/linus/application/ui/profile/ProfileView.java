package com.linus.application.ui.profile;

import com.linus.application.backend.friend.FriendService;
import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.security.AuthenticatedUser;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "profile", layout = com.linus.application.ui.MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class ProfileView extends VerticalLayout implements LocaleChangeObserver, HasDynamicTitle {

    private final AuthenticatedUser authenticatedUser;
    private final FriendService friendService;
    private final H2 title;
    private final Paragraph usernameField;
    private final Paragraph friendCodeField;

    public ProfileView(AuthenticatedUser authenticatedUser, FriendService friendService) {
        this.authenticatedUser = authenticatedUser;
        this.friendService = friendService;

        setPadding(true);
        setSpacing(true);

        title = new H2();
        usernameField = new Paragraph();
        friendCodeField = new Paragraph();

        User user = authenticatedUser.get().orElseThrow();
        usernameField.setText(getTranslation("profile.username") + ": " + user.getUsername());
        friendCodeField.setText(getTranslation("profile.friendCode") + ": " + user.getFriendCode());

        add(title, usernameField, friendCodeField);
        updateTexts();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
    }

    private void updateTexts() {
        title.setText(getTranslation("profile.title"));
        User user = authenticatedUser.get().orElse(null);
        if (user != null) {
            usernameField.setText(getTranslation("profile.username") + ": " + user.getUsername());
            friendCodeField.setText(getTranslation("profile.friendCode") + ": " + user.getFriendCode());
        }
    }

    @Override
    public String getPageTitle() {
        return getTranslation("profile.title");
    }
}
