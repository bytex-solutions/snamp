package com.itworks.snamp.connectors.notifications;

import com.itworks.snamp.SynchronizationEvent;

import java.util.Objects;

/**
 * Represents utility methods that simplifies working with notifications.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class NotificationUtils {

    private NotificationUtils(){

    }

    /**
     * Represents notification listener that can be used to handle the notification
     * synchronously. This class cannot be inherited.
     * <p>
     *     The following example demonstrates how to use this class:
     *     <pre>{@code
     *     final SynchronizationListener listener = new SynchronizationListener();
     *     final Object listenerId = connector.subscribe("subs-list", listener);
     *     listener.getAwaitor().await(); //blocks the caller thread
     *     connector.unsubscribe(listenerId);
     *     }</pre>
     * </p>
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class SynchronizationListener extends SynchronizationEvent<Notification> implements NotificationListener{
        private static final String ANY_SUBSCRIPTION_LIST = "*";
        private final String expectedSubscriptionList;

        /**
         * Initializes a new synchronizer with the specified filter.
         * @param subscriptionList An identifier of the expected subscription list.
         */
        public SynchronizationListener(final String subscriptionList){
            super(true);
            expectedSubscriptionList = subscriptionList == null || subscriptionList.isEmpty() ?
                    ANY_SUBSCRIPTION_LIST: subscriptionList;
        }

        /**
         * Initializes a new synchronizer for the notification delivery process.
         */
        public SynchronizationListener(){
            this(ANY_SUBSCRIPTION_LIST);
        }

        /**
         * Handles the specified notification.
         *
         * @param listId An identifier of the subscription list.
         * @param n        The notification to handle.
         * @return {@literal true}, if notification is handled successfully; otherwise, {@literal false}.
         */
        @Override
        public boolean handle(final String listId, final Notification n) {
            return (Objects.equals(listId, expectedSubscriptionList) ||
                    Objects.equals(expectedSubscriptionList, ANY_SUBSCRIPTION_LIST)) && fire(n);
        }
    }

    /**
     * Computes an identifier for the specified notification listener.
     * @param listener Notification listener for which ID should be generated.
     * @return An identifier of the notification listener.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static String generateListenerId(final NotificationListener listener){
        return listener != null ? Integer.toString(listener.hashCode()) : null;
    }

    private static String prepareTopicName(final String topicName){
        final char[] chars = topicName.toCharArray();
        for(int i = 0; i < chars.length; i++){
            final char ch = chars[i];
            if(ch != '/' && ch != '_' && ch != '-' &&
                    !(ch >= '0' && ch <= '9') &&
                    !(ch >= 'a' && ch <= 'z') &&
                    !(ch >= 'A' && ch <= 'Z')) chars[i] = '_';
        }
        return new String(chars);
    }

    /**
     * Constructs topic name for the {@link org.osgi.service.event.Event} that represents SNAMP notification.
     * @param connectorName The management connector name.
     * @param notificationCategory The notification category.
     * @param subscriptionList The subscription list identifier.
     * @return The event topic name.
     */
    public static String getTopicName(final String connectorName,
                                      final String notificationCategory,
                                      final String subscriptionList){
        return prepareTopicName(String.format("com/itworks/snamp/%s/%s/%s", connectorName, notificationCategory, subscriptionList));
    }

    /**
     * Constructs topic name for the {@link org.osgi.service.event.Event} that represents SNAMP notification.
     * @param connectorName The management connector name.
     * @param metadata The notification descriptor.
     * @param subscriptionList The subscription list identifier.
     * @return The event topic name.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static String getTopicName(final String connectorName,
                                      final NotificationMetadata metadata,
                                      final String subscriptionList){
        return getTopicName(connectorName, metadata.getCategory(), subscriptionList);
    }
}
