package com.bytex.snamp.connector.notifications;

import com.bytex.snamp.Convert;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.Communicator;
import com.bytex.snamp.core.Exchange;

import javax.management.Notification;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class NotificationExchange extends Exchange<Notification> {
    private NotificationExchange(final ClusterMember clusterMember,
                                   final String channelName,
                                   final String resourceName) {
        super(clusterMember, channelName, notificationFilter(resourceName), Notification.class);
    }

    private static Predicate<? super Communicator.MessageEvent> notificationFilter(final String resourceName) {
        return message -> message.isRemote() &&
                Convert.toType(message.getPayload(), Notification.class)
                        .filter(payload -> Objects.equals(resourceName, payload.getSource()))
                        .isPresent();
    }

    public static NotificationExchange create(final ClusterMember clusterMember,
                                              final String channelName,
                                              final String resourceName,
                                              final Consumer<? super Notification> receiver) {
        return new NotificationExchange(clusterMember, channelName, resourceName) {
            @Override
            public void accept(final Notification payload) {
                receiver.accept(payload);
            }
        };
    }
}
