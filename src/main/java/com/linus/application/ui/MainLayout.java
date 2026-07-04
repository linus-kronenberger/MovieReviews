package com.linus.application.ui;

import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.security.AuthenticatedUser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@AnonymousAllowed
@Layout
public class MainLayout extends AppLayout implements LocaleChangeObserver {

    private final AuthenticatedUser authenticatedUser;
    private final H1 title;
    private final SideNav sideNav;
    private final List<SideNavItem> navItems = new ArrayList<>();
    private Footer footer;

    public MainLayout(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;

        DrawerToggle toggle = new DrawerToggle();

        title = new H1();
        title.getStyle().set("font-size", "1.125rem").set("margin", "0");

        sideNav = createSideNav();

        Scroller scroller = new Scroller(sideNav);

        addToDrawer(scroller);
        addToNavbar(toggle, title);
        buildFooter();
        updateTexts();
        restoreLocale();
    }

    private SideNav createSideNav() {
        SideNav nav = new SideNav();

        navItems.add(new SideNavItem("", "/"));
        navItems.add(new SideNavItem("", "/add-movie"));
        navItems.add(new SideNavItem("", "/add-rating"));
        navItems.add(new SideNavItem("", "/manage-categories"));
        navItems.add(new SideNavItem("", "/friends"));
        navItems.add(new SideNavItem("", "/profile"));
        nav.addItem(navItems.toArray(new SideNavItem[0]));

        nav.getStyle().set("margin", "var(--vaadin-gap-s)");
        return nav;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
    }

    private void updateTexts() {
        title.setText(getTranslation("mainlayout.title"));
        navItems.get(0).setLabel(getTranslation("mainlayout.nav.movies"));
        navItems.get(1).setLabel(getTranslation("mainlayout.nav.newMovie"));
        navItems.get(2).setLabel(getTranslation("mainlayout.nav.review"));
        navItems.get(3).setLabel(getTranslation("mainlayout.nav.categories"));
        navItems.get(4).setLabel(getTranslation("mainlayout.nav.friends"));
        navItems.get(5).setLabel(getTranslation("mainlayout.nav.profile"));
        refreshFooter();
    }

    private void buildFooter() {
        footer = createFooter();
        addToDrawer(footer);
    }

    private void refreshFooter() {
        if (footer != null) {
            remove(footer);
        }
        buildFooter();
    }

    private void restoreLocale() {
        UI.getCurrent().getPage().executeJs(
            "return window.localStorage.getItem('locale')")
            .then(String.class, stored -> {
                if (stored != null && !stored.isEmpty()) {
                    UI.getCurrent().setLocale(Locale.forLanguageTag(stored));
                }
            });
    }

    private Footer createFooter() {
        Footer layout = new Footer();
        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();
            Avatar avatar = new Avatar(user.getName());
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");
            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(user.getName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().setDisplay(Style.Display.FLEX);
            div.getElement().getStyle().setAlignItems(Style.AlignItems.CENTER);
            div.getElement().getStyle().setGap("var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem(getTranslation("mainlayout.logout"), e -> {
                authenticatedUser.logout();
            });
            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", getTranslation("mainlayout.login"));
            layout.add(loginLink);
        }

        Select<Locale> localeSelect = new Select<>();
        localeSelect.setItems(Locale.GERMAN, Locale.ENGLISH);
        localeSelect.setValue(UI.getCurrent().getLocale());
        localeSelect.setItemLabelGenerator(locale -> locale.getDisplayLanguage(locale));
        localeSelect.addValueChangeListener(e -> {
            String code = e.getValue().toLanguageTag();
            UI.getCurrent().getPage().executeJs(
                "window.localStorage.setItem('locale', $0)", code);
            UI.getCurrent().setLocale(e.getValue());
        });
        HorizontalLayout localeRow = new HorizontalLayout(localeSelect);
        localeRow.getStyle().set("padding", "var(--lumo-space-s)");
        layout.add(localeRow);

        return layout;
    }
}
