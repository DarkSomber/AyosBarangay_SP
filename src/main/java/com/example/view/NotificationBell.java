package com.example.view;

import java.time.format.DateTimeFormatter;

import org.springframework.dao.DataAccessException;

import com.example.controller.NotificationController;
import com.example.model.AdminNotification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;

public class NotificationBell extends Div {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("MMM d, h:mm a");

    private final NotificationController.Source source;
    private final Span dot = new Span();

    public NotificationBell(NotificationController.Source source) {
        this.source = source;
        addClassName("notif-bell");

        Button bell = new Button(VaadinIcon.BELL.create(), e -> openList());
        bell.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        bell.setAriaLabel("Notifications");
        dot.addClassName("notif-dot");
        add(bell, dot);
        refresh();
    }

    public void refresh() {
        try {
            dot.setVisible(source.unreadCount() > 0);
        } catch (DataAccessException ex) {
            dot.setVisible(false);
        }
    }

    private void openList() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Notifications");
        dialog.setWidth("440px");
        dialog.setMaxWidth("95vw");

        Div list = new Div();
        fill(list);
        dialog.add(list);

        Button markAll = new Button("Mark all as read", e -> {
            run(() -> source.markAllRead());
            fill(list);
        });
        dialog.getFooter().add(markAll, new Button("Close", e -> dialog.close()));
        dialog.open();
    }

    private void fill(Div list) {
        list.removeAll();
        try {
            var items = source.recent();
            if (items.isEmpty()) {
                list.add(new Span("No notifications."));
            }
            items.forEach(n -> list.add(row(n, list)));
        } catch (DataAccessException ex) {
            list.add(new Span("Notifications could not be loaded."));
        }
        refresh();
    }

    private Div row(AdminNotification n, Div list) {
        boolean unread = n.status() == AdminNotification.Status.UNREAD;

        Span message = new Span(n.message());
        message.addClassName("notif-message");
        Span time = new Span(n.createdAt().format(WHEN));
        time.addClassName("notif-time");

        Div actions = new Div();
        actions.addClassName("notif-actions");
        Button markRead = new Button("Mark as read", e -> {
            run(() -> source.markRead(n.notificationId()));
            fill(list);
        });
        markRead.setEnabled(unread);
        actions.add(markRead);
        actions.add(new Button("Ignore", e -> {
            run(() -> source.ignore(n.notificationId()));
            fill(list);
        }));

        Div row = new Div(new Div(message), new Div(time), actions);
        row.addClassName("notif-item");
        row.setClassName("notif-unread", unread);
        return row;
    }

    private void run(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | DataAccessException ex) {
            Notification.show("Could not update the notification.");
        }
    }
}