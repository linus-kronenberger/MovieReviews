package com.linus.application.ui.util.components.notifications;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public class WarningNotification extends Notification {
  public WarningNotification(String message) {
    super(message);
    this.addThemeVariants(NotificationVariant.WARNING);
  }
}
