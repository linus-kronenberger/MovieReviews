package com.linus.application.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;

@Layout
public class MainLayout extends AppLayout {
    public MainLayout() {
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("Filme");
        title.getStyle().set("font-size", "1.125rem").set("margin", "0");

        SideNav sideNav = getSideNav();

        Scroller scroller = new Scroller(sideNav);

        addToDrawer(scroller);
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
}
