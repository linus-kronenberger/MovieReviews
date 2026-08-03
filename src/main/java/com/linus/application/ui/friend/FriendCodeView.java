package com.linus.application.ui.friend;

import com.linus.application.backend.friend.FriendRequest;
import com.linus.application.backend.friend.FriendService;
import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.security.AuthenticatedUser;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route(value = "friends", layout = com.linus.application.ui.MainLayout.class)
@RolesAllowed({"USER", "ADMIN"})
public class FriendCodeView extends VerticalLayout implements LocaleChangeObserver, HasDynamicTitle {

    private final FriendService friendService;
    private final AuthenticatedUser authenticatedUser;
    private final H2 title;
    private final H2 profileTitle;
    private final Paragraph usernameField;
    private final Button addFriendButton;
    private final Grid<User> friendsGrid;
    private final Grid<FriendRequest> requestsGrid;

    public FriendCodeView(FriendService friendService, AuthenticatedUser authenticatedUser) {
        this.friendService = friendService;
        this.authenticatedUser = authenticatedUser;

        setPadding(true);
        setSpacing(true);

        title = new H2();
        profileTitle = new H2();
        usernameField = new Paragraph();

        addFriendButton = new Button();
        addFriendButton.addClickListener(e -> {
            FriendCodeDialog dialog = new FriendCodeDialog(friendService, authenticatedUser, this::refreshData);
            dialog.open();
        });

        friendsGrid = new Grid<>(User.class, false);
        friendsGrid.addColumn(User::getUsername).setAutoWidth(true);
        friendsGrid.setAllRowsVisible(true);

        requestsGrid = new Grid<>(FriendRequest.class, false);
        requestsGrid.addColumn(r -> r.getFromUser().getUsername()).setAutoWidth(true);
        requestsGrid.setAllRowsVisible(true);

        add(title);
        add(profileTitle);
        add(usernameField);
        add(addFriendButton);
        add(new Paragraph(getTranslation("friend.friends")));
        add(friendsGrid);
        add(new Paragraph(getTranslation("friend.requests")));
        add(requestsGrid);

        updateTexts();
        refreshData();
    }

    private void refreshData() {
        User currentUser = authenticatedUser.get().orElse(null);
        if (currentUser == null) return;

        List<User> friends = friendService.getFriends(currentUser);
        friendsGrid.setItems(friends);

        List<FriendRequest> requests = friendService.getReceivedRequests(currentUser);
        requestsGrid.setItems(requests);
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
    }

    private void updateTexts() {
        title.setText(getTranslation("friend.title"));
        profileTitle.setText(getTranslation("friend.profileTitle"));
        addFriendButton.setText(getTranslation("friend.addFriend"));
        User user = authenticatedUser.get().orElse(null);
        if (user != null) {
            usernameField.setText(getTranslation("friend.username") + ": " + user.getUsername());
        }
        if (!friendsGrid.getColumns().isEmpty()) {
            friendsGrid.getColumns().get(0).setHeader(getTranslation("friend.friends"));
        }
        if (!requestsGrid.getColumns().isEmpty()) {
            requestsGrid.getColumns().get(0).setHeader(getTranslation("friend.requests"));
        }
    }

    @Override
    public String getPageTitle() {
        return getTranslation("friend.title");
    }
}
