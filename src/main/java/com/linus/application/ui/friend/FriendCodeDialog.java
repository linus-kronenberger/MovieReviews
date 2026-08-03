package com.linus.application.ui.friend;

import com.linus.application.backend.friend.FriendService;
import com.linus.application.backend.user.infrastructure.User;
import com.linus.application.security.AuthenticatedUser;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;

public class FriendCodeDialog extends Dialog implements LocaleChangeObserver {

    private final FriendService friendService;
    private final AuthenticatedUser authenticatedUser;
    private final Runnable onFriendRequestSent;
    private final Paragraph friendCodeDisplay;
    private final Paragraph info;
    private final TextField friendCodeInput;
    private final Button sendButton;

    public FriendCodeDialog(FriendService friendService, AuthenticatedUser authenticatedUser, Runnable onFriendRequestSent) {
        this.friendService = friendService;
        this.authenticatedUser = authenticatedUser;
        this.onFriendRequestSent = onFriendRequestSent;

        friendCodeDisplay = new Paragraph();
        info = new Paragraph();

        friendCodeInput = new TextField();
        friendCodeInput.setMaxLength(8);
        friendCodeInput.setWidth("300px");

        sendButton = new Button();
        sendButton.addClickListener(e -> sendFriendRequest());

        VerticalLayout layout = new VerticalLayout(friendCodeDisplay, info, friendCodeInput, sendButton);
        layout.setPadding(true);
        layout.setSpacing(true);
        add(layout);

        updateTexts();
    }

    private void sendFriendRequest() {
        String code = friendCodeInput.getValue();
        if (code == null || code.isBlank()) {
            Notification.show(getTranslation("friend.error.codeRequired"));
            return;
        }
        String result = friendService.sendFriendRequest(code.toUpperCase());
        Notification.show(getTranslation(result));
        friendCodeInput.clear();
        onFriendRequestSent.run();
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        updateTexts();
    }

    private void updateTexts() {
        info.setText(getTranslation("friend.info"));
        friendCodeInput.setLabel(getTranslation("friend.codeLabel"));
        sendButton.setText(getTranslation("friend.send"));
        User user = authenticatedUser.get().orElse(null);
        if (user != null) {
            friendCodeDisplay.setText(getTranslation("friend.friendCode") + ": " + user.getFriendCode());
        }
    }
}
