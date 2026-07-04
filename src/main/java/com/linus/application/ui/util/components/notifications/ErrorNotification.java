package com.linus.application.ui.util.components.notifications;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public class ErrorNotification extends Notification {
  public ErrorNotification(String message) {
    super(message);
    this.addThemeVariants(NotificationVariant.ERROR);
  }
}
