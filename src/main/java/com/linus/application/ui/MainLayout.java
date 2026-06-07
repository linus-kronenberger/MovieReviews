package com.linus.application.ui;

import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.security.AuthenticatedUser;
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
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.Optional;

@AnonymousAllowed
@Layout
public class MainLayout extends AppLayout {

    private final AuthenticatedUser authenticatedUser;

    public MainLayout(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;

        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("Filme");
        title.getStyle().set("font-size", "1.125rem").set("margin", "0");

        SideNav sideNav = getSideNav();

        Scroller scroller = new Scroller(sideNav);

        addToDrawer(scroller, createFooter());
        addToNavbar(toggle, title);
    }

    public SideNav getSideNav() {
        SideNav sideNav = new SideNav();

        sideNav.addItem(new SideNavItem("Filme", "/"),
                new SideNavItem("Neue Bewertung", "/add-rating")
        );

        sideNav.getStyle().set("margin", "var(--vaadin-gap-s)");
        return sideNav;
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
            userName.getSubMenu().addItem("Abmelden", e -> {
                authenticatedUser.logout();
            });
            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Anmelden");
            layout.add(loginLink);
        }
        return layout;
    }
}
