package com.linus.application.ui.statistics;

import com.linus.application.backend.essay.infrastructure.Essay;
import com.linus.application.backend.friend.FriendService;
import com.linus.application.backend.movie.infrastructure.MovieService;
import com.linus.application.backend.review.infrastructure.Review;
import com.linus.application.backend.review.infrastructure.ReviewService;
import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.security.AuthenticatedUser;
import com.linus.application.ui.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "statistics", layout = MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class StatisticsView extends VerticalLayout implements LocaleChangeObserver {

    private final ReviewService reviewService;
    private final MovieService movieService;
    private final FriendService friendService;
    private final AuthenticatedUser authenticatedUser;

    private final H1 h1;
    private final VerticalLayout myTop10Section;
    private final VerticalLayout friendsSection;
    private final VerticalLayout allRatingsSection;
    private final Grid<Review> allRatingsGrid;
    private Map<String, String> essayMap;

    public StatisticsView(ReviewService reviewService, MovieService movieService,
                          FriendService friendService, AuthenticatedUser authenticatedUser) {
        this.reviewService = reviewService;
        this.movieService = movieService;
        this.friendService = friendService;
        this.authenticatedUser = authenticatedUser;

        setWidthFull();
        setPadding(true);
        setSpacing(true);

        h1 = new H1();

        myTop10Section = new VerticalLayout();
        myTop10Section.setPadding(false);
        myTop10Section.setSpacing(true);

        friendsSection = new VerticalLayout();
        friendsSection.setPadding(false);
        friendsSection.setSpacing(true);

        allRatingsSection = new VerticalLayout();
        allRatingsSection.setPadding(false);
        allRatingsSection.setSpacing(true);

        allRatingsGrid = new Grid<>(Review.class, false);
        allRatingsGrid.addColumn(r -> r.getMovie().getTitle())
                .setKey("movie");
        allRatingsGrid.addColumn(r -> r.getUser().getUsername())
                .setKey("user");
        allRatingsGrid.addColumn(r -> String.format("%.1f", r.getAverageRating()))
                .setKey("rating");
        allRatingsGrid.addColumn(r -> {
            String text = essayMap != null ? essayMap.get(r.getId()) : null;
            if (text != null && !text.isBlank()) {
                return text.length() > 80 ? text.substring(0, 80) + "..." : text;
            }
            return "-";
        }).setKey("essay");

        allRatingsGrid.addItemClickListener(event -> {
            if (event.getItem() != null) {
                Review review = event.getItem();
                String essayText = essayMap != null ? essayMap.get(review.getId()) : null;
                RatingDetailDialog dialog = new RatingDetailDialog(
                        review, reviewService, movieService, this::getTranslation, essayText);
                dialog.open();
            }
        });

        add(h1, myTop10Section, friendsSection, allRatingsSection);
        refreshData();
        updateTexts();
    }

    private void refreshData() {
        myTop10Section.removeAll();
        friendsSection.removeAll();
        allRatingsSection.removeAll();

        User currentUser = authenticatedUser.get().orElse(null);
        if (currentUser == null) return;

        List<Review> allReviews = reviewService.getAllReviewsForUserAndFriends();

        List<Essay> essays = reviewService.getEssaysByRatings(allReviews);
        essayMap = essays.stream()
                .collect(Collectors.toMap(e -> e.getRating().getId(), Essay::getText));

        List<User> friends = friendService.getFriends(currentUser);

        buildMyTop10Section(currentUser, allReviews);
        buildFriendsSections(friends, allReviews);
        buildAllRatingsGrid(allReviews);
    }

    private void buildMyTop10Section(User currentUser, List<Review> allReviews) {
        H2 myTop10Header = new H2(getTranslation("statistics.myTop10"));
        myTop10Section.add(myTop10Header);

        List<Review> myTop = allReviews.stream()
                .filter(r -> r.getUser().getId().equals(currentUser.getId()))
                .sorted(Comparator.comparingDouble(Review::getAverageRating).reversed())
                .limit(10)
                .toList();

        if (myTop.isEmpty()) {
            myTop10Section.add(new Span(getTranslation("statistics.noReviews")));
        } else {
            myTop10Section.add(buildCardsRow(myTop));
        }
    }

    private void buildFriendsSections(List<User> friends, List<Review> allReviews) {
        for (User friend : friends) {
            String friendName = friend.getName() != null ? friend.getName() : friend.getUsername();
            H2 friendHeader = new H2(getTranslation("statistics.friendsTop", friendName));
            friendsSection.add(friendHeader);

            List<Review> friendTop = allReviews.stream()
                    .filter(r -> r.getUser().getId().equals(friend.getId()))
                    .sorted(Comparator.comparingDouble(Review::getAverageRating).reversed())
                    .limit(10)
                    .toList();

            if (friendTop.isEmpty()) {
                friendsSection.add(new Span(getTranslation("statistics.noFriendsTop", friendName)));
            } else {
                friendsSection.add(buildCardsRow(friendTop));
            }
        }
    }

    private void buildAllRatingsGrid(List<Review> allReviews) {
        H2 allRatingsHeader = new H2(getTranslation("statistics.allRatings"));
        allRatingsSection.add(allRatingsHeader);

        if (allReviews.isEmpty()) {
            allRatingsSection.add(new Span(getTranslation("statistics.noReviews")));
        } else {
            allRatingsGrid.setItems(allReviews);
            allRatingsSection.add(allRatingsGrid);
        }
    }

    private HorizontalLayout buildCardsRow(List<Review> reviews) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.getStyle().set("overflow-x", "auto");
        row.getStyle().set("overflow-y", "hidden");
        row.getStyle().set("white-space", "nowrap");
        row.getStyle().set("padding", "var(--lumo-space-s) 0");
        row.setSpacing(true);

        for (int i = 0; i < reviews.size(); i++) {
            Review review = reviews.get(i);
            VerticalLayout card = createCard(i + 1, review);
            row.add(card);
        }

        return row;
    }

    private VerticalLayout createCard(int rank, Review review) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("200px");
        card.setPadding(true);
        card.setSpacing(false);
        card.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        card.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        card.getStyle().set("box-shadow", "var(--lumo-box-shadow-xs)");
        card.getStyle().set("background", "var(--lumo-base-color)");
        card.getStyle().set("flex-shrink", "0");
        card.setAlignItems(FlexComponent.Alignment.CENTER);

        Span rankBadge = new Span("#" + rank);
        rankBadge.getStyle().set("font-weight", "bold");
        rankBadge.getStyle().set("font-size", "1.2rem");
        rankBadge.getStyle().set("color", "var(--lumo-primary-color)");

        String posterUrl = null;
        if (review.getMovie().getImdbId() != null && !review.getMovie().getImdbId().isBlank()) {
            posterUrl = movieService.fetchPosterUrl(review.getMovie().getImdbId());
        }

        Image poster = new Image();
        if (posterUrl != null) {
            poster.setSrc(posterUrl);
        } else {
            poster.setVisible(false);
        }
        poster.setAlt(getTranslation("statistics.rank", rank) + " - " + review.getMovie().getTitle());
        poster.setMaxWidth("160px");
        poster.setMaxHeight("220px");

        Span title = new Span(review.getMovie().getTitle());
        title.getStyle().set("font-weight", "500");
        title.getStyle().set("text-align", "center");
        title.getStyle().set("font-size", "0.9rem");
        title.getStyle().set("width", "100%");
        title.getStyle().set("overflow", "hidden");
        title.getStyle().set("text-overflow", "ellipsis");
        title.getStyle().set("white-space", "nowrap");

        Span ratingSpan = new Span(String.format("%.1f ★", review.getAverageRating()));
        ratingSpan.getStyle().set("color", "var(--lumo-primary-color)");
        ratingSpan.getStyle().set("font-weight", "bold");

        String essayText = essayMap != null ? essayMap.get(review.getId()) : null;
        Span essaySpan = null;
        if (essayText != null && !essayText.isBlank()) {
            String truncated = essayText.length() > 100 ? essayText.substring(0, 100) + "..." : essayText;
            essaySpan = new Span(truncated);
            essaySpan.getStyle().set("font-size", "0.75rem");
            essaySpan.getStyle().set("color", "var(--lumo-secondary-text-color)");
            essaySpan.getStyle().set("text-align", "center");
            essaySpan.getStyle().set("max-height", "60px");
            essaySpan.getStyle().set("overflow", "hidden");
        }

        card.add(rankBadge, poster, title, ratingSpan);
        if (essaySpan != null) {
            card.add(essaySpan);
        }

        String essayTextForDialog = essayText;
        card.addClickListener(event -> {
            RatingDetailDialog dialog = new RatingDetailDialog(
                    review, reviewService, movieService, this::getTranslation, essayTextForDialog);
            dialog.open();
        });

        return card;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
        refreshData();
    }

    private void updateTexts() {
        h1.setText(getTranslation("statistics.title"));
        if (allRatingsGrid.getColumnByKey("movie") != null)
            allRatingsGrid.getColumnByKey("movie").setHeader(getTranslation("statistics.grid.movie"));
        if (allRatingsGrid.getColumnByKey("user") != null)
            allRatingsGrid.getColumnByKey("user").setHeader(getTranslation("statistics.grid.user"));
        if (allRatingsGrid.getColumnByKey("rating") != null)
            allRatingsGrid.getColumnByKey("rating").setHeader(getTranslation("statistics.grid.rating"));
        if (allRatingsGrid.getColumnByKey("essay") != null)
            allRatingsGrid.getColumnByKey("essay").setHeader(getTranslation("statistics.grid.essay"));
    }
}
