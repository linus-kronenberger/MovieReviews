package com.linus.application.ui.statistics;

import com.linus.application.backend.movie.infrastructure.Movie;
import com.linus.application.backend.movie.infrastructure.MovieService;
import com.linus.application.backend.review.infrastructure.Review;
import com.linus.application.backend.review.infrastructure.ReviewService;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

import java.util.Map;
import java.util.function.Function;

public class RatingDetailDialog extends Dialog implements LocaleChangeObserver {

    private final Review review;
    private final ReviewService reviewService;
    private final MovieService movieService;
    private final Function<String, String> translator;
    private final VerticalLayout contentLayout = new VerticalLayout();
    private final String essayText;
    private TextArea essayArea;
    private VerticalLayout ratingsLayout;

    public RatingDetailDialog(Review review, ReviewService reviewService,
                              MovieService movieService, Function<String, String> translator,
                              String essayText) {
        this.review = review;
        this.reviewService = reviewService;
        this.movieService = movieService;
        this.translator = translator;
        this.essayText = essayText;

        setWidth("600px");
        setHeight("80vh");

        contentLayout.setPadding(false);
        contentLayout.setSpacing(true);
        add(contentLayout);

        buildContent();
        updateTexts();
    }

    private void buildContent() {
        Movie movie = review.getMovie();

        Span userSpan = new Span();
        userSpan.getStyle().set("font-weight", "500");
        userSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");

        Span avgSpan = new Span();
        avgSpan.getStyle().set("font-weight", "bold");
        avgSpan.getStyle().set("color", "var(--lumo-primary-color)");

        HorizontalLayout infoRow = new HorizontalLayout(userSpan, avgSpan);
        infoRow.setWidthFull();
        infoRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        contentLayout.add(infoRow);

        HorizontalLayout posterLayout = new HorizontalLayout();
        posterLayout.setWidthFull();
        posterLayout.setAlignItems(FlexComponent.Alignment.STRETCH);

        if (movie.getImdbId() != null && !movie.getImdbId().isBlank()) {
            String posterUrl = movieService.fetchPosterUrl(movie.getImdbId());
            if (posterUrl != null) {
                Image poster = new Image(posterUrl, movie.getTitle());
                poster.setMaxWidth("150px");
                poster.setMaxHeight("250px");
                posterLayout.add(poster);
            }
        }

        TextArea descriptionArea = new TextArea();
        descriptionArea.setValue(movie.getDescription() != null ? movie.getDescription() : "");
        descriptionArea.setReadOnly(true);
        descriptionArea.setHeight("200px");
        descriptionArea.setWidthFull();
        posterLayout.add(descriptionArea);
        posterLayout.setFlexGrow(1, descriptionArea);

        contentLayout.add(posterLayout);

        ratingsLayout = new VerticalLayout();
        ratingsLayout.setPadding(false);
        ratingsLayout.setSpacing(true);
        Span ratingsHeader = new Span();
        ratingsHeader.getStyle().set("font-weight", "bold");
        ratingsLayout.add(ratingsHeader);

        Map<String, Double> breakdown = reviewService.getRatingsBreakdown(review);
        for (Map.Entry<String, Double> entry : breakdown.entrySet()) {
            HorizontalLayout ratingRow = new HorizontalLayout();
            ratingRow.setWidthFull();
            ratingRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            Span categorySpan = new Span(entry.getKey());
            Span starsSpan = new Span(String.format("%.1f ★", entry.getValue()));
            starsSpan.getStyle().set("font-weight", "bold");
            ratingRow.add(categorySpan, starsSpan);
            ratingsLayout.add(ratingRow);
        }

        contentLayout.add(ratingsLayout);

        essayArea = new TextArea();
        essayArea.setReadOnly(true);
        essayArea.setWidthFull();
        essayArea.setMinHeight("100px");
        essayArea.setMaxHeight("200px");
        essayArea.getStyle().set("overflow-y", "auto");
        if (essayText != null && !essayText.isBlank()) {
            essayArea.setValue(essayText);
        }
        contentLayout.add(essayArea);

        userSpan.setText(translator.apply("statistics.detail.user") + ": " + review.getUser().getUsername());
        avgSpan.setText(translator.apply("statistics.detail.rating") + ": " + String.format("%.1f", review.getAverageRating()));
        ratingsHeader.setText(translator.apply("statistics.detail.categories"));
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
    }

    private void updateTexts() {
        Movie movie = review.getMovie();
        String typeLabel = movie.getType() != null ? movie.getType().getDisplayName() : "";
        setHeaderTitle(movie.getTitle() + (typeLabel.isEmpty() ? "" : " (" + typeLabel + ")"));
    }
}
