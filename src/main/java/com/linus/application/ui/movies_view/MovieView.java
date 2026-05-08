package com.linus.application.ui.movies_view;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * This is the MovieView.
 * Paginating in Vaadin / Spring Boot:
 * https://vaadin.com/docs/latest/building-apps/forms-data/add-grid/paginated-data
 */
@Route("")
public class MovieView extends VerticalLayout {

    /**
     * This is the Main View of the App :D
     *
     */
    public MovieView() {
        // Define DOM
        H1 h1 = new H1("Movies");

        // Add the components
        add(h1);
    }
}
