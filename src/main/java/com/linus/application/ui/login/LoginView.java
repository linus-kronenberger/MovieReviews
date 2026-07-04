package com.linus.application.ui.login;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "login")
@AnonymousAllowed
public class LoginView extends LoginOverlay implements LocaleChangeObserver, HasDynamicTitle {

    private final Button registerButton;

    public LoginView() {
        setAction("login");

        setForgotPasswordButtonVisible(false);

        registerButton = new Button();
        registerButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("register")));
        registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        registerButton.getStyle().set("margin-top", "var(--lumo-space-s)");
        getFooter().add(registerButton);

        setOpened(true);
        updateTexts();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
    }

    private void updateTexts() {
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle(getTranslation("login.headerTitle"));
        i18n.getHeader().setDescription(getTranslation("login.headerDescription"));
        i18n.setAdditionalInformation(null);
        setI18n(i18n);
        registerButton.setText(getTranslation("login.register"));
    }

    @Override
    public String getPageTitle() {
        return getTranslation("login.pageTitle");
    }
}
