package com.itworks.snamp.connectors.notifications;

import com.itworks.snamp.SynchronizationEvent;
import org.apache.commons.collections4.MapUtils;
import org.osgi.service.event.Event;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents utility methods that simplifies working with notifications.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class NotificationUtils {
    /**
     * Represents SNAMP notification constructed from {@link org.osgi.service.event.Event} object.
     * This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class NotificationEvent extends NotificationImpl{
        private static final String TIME_STAMP_EVENT_PROPERTY = "timeStamp";
        private static final String MESSAGE_EVENT_PROPERTY = "message";
        private static final String SEQ_NUM_EVENT_PROPERTY = "sequenceNumber";
        private static final String SEVERITY_EVENT_PROPERTY = "severity";
        private static final String LIST_EVENT_PROPERTY = "subscriptionListID";
        private static final String EMITTER_EVENT_PROPERTY = "emitter";

        /**
         * Initializes a new instance of the SNAMP notification using {@link org.osgi.service.event.Event} object.
         * @param ev An event to parse.
         */
        @SuppressWarnings("UnusedDeclaration")
        public NotificationEvent(final Event ev){
            super( getEventProperty(ev, SEVERITY_EVENT_PROPERTY, Severity.class, Severity.UNKNOWN),
                    getEventProperty(ev, SEQ_NUM_EVENT_PROPERTY, Long.class, 0L),
                    getEventProperty(ev, TIME_STAMP_EVENT_PROPERTY, Date.class, new Date()),
                    getEventProperty(ev, MESSAGE_EVENT_PROPERTY, String.class, ""));
            //parse attachments
            for(final String propertyName: ev.getPropertyNames())
                switch (propertyName){
                    default: put(propertyName, ev.getProperty(propertyName));
                    case SEVERITY_EVENT_PROPERTY:
                    case SEQ_NUM_EVENT_PROPERTY:
                    case TIME_STAMP_EVENT_PROPERTY:
                    case MESSAGE_EVENT_PROPERTY:
                }
        }

        /**
         * Initializes a new instance of the SNAMP notification.
         * @param notification Original SNAMP notification to wrap.
         * @param resourceName The name of the managed resource which emits the event.
         * @param listId An identifier of the subscription list.
         */
        public NotificationEvent(final Notification notification, final String resourceName, final String listId){
            super(notification);
            put(LIST_EVENT_PROPERTY, listId);
            put(EMITTER_EVENT_PROPERTY, resourceName);
        }

        /**
         * Gets name of the managed resource which emits this event.
         * @return The name of the manager resource.
         */
        public String getEmitter(){
            return MapUtils.getString(this, EMITTER_EVENT_PROPERTY);
        }


        /**
         * Gets subscription list identifier.
         * @return The subscription list identifier.
         */
        public String getSubscriptionListID(){
            return MapUtils.getString(this, LIST_EVENT_PROPERTY);
        }

        /**
         * Wraps notification into {@link org.osgi.service.event.Event} object.
         * @param connectorName The management connector name.
         * @param category An event category.
         * @return A new instance of the {@link org.osgi.service.event.Event} object that
         *          contains properties from this notification.
         */
        public Event toEvent(final String connectorName,
                             final String category){
            final Map<String, Object> eventProps = new HashMap<>(10);
            eventProps.put(TIME_STAMP_EVENT_PROPERTY, getTimeStamp());
            eventProps.put(MESSAGE_EVENT_PROPERTY, getMessage());
            eventProps.put(SEQ_NUM_EVENT_PROPERTY, getSequenceNumber());
            eventProps.put(SEVERITY_EVENT_PROPERTY, getSeverity());
            eventProps.put(LIST_EVENT_PROPERTY, getSubscriptionListID());
            eventProps.put(EMITTER_EVENT_PROPERTY, getEmitter());
            //attach events
            for(final String attachmentName: keySet())
                switch (attachmentName){
                    default: eventProps.put(attachmentName, get(attachmentName));
                    case SEVERITY_EVENT_PROPERTY:
                    case SEQ_NUM_EVENT_PROPERTY:
                    case TIME_STAMP_EVENT_PROPERTY:
                    case MESSAGE_EVENT_PROPERTY:
                    case EMITTER_EVENT_PROPERTY:
                    case LIST_EVENT_PROPERTY:
                }
            return new Event(getTopicName(connectorName, category, getSubscriptionListID()),
                    eventProps);
        }
    }

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

    /**
     * Reads event property.
     * @param ev An event to parse.
     * @param propertyName The name of the event property to read.
     * @param propertyType The type of the event property.
     * @param defaultValue The default value if the property is not available.
     * @param <T> Type of the property to read.
     * @return The value of the property; or default value.
     */
    public static <T> T getEventProperty(final Event ev,
                                         final String propertyName,
                                         final Class<T> propertyType,
                                         final T defaultValue){
        if(ev == null) return defaultValue;
        else if(ev.containsProperty(propertyName)){
            final Object result = ev.getProperty(propertyName);
            return propertyType.isInstance(result) ? propertyType.cast(result) : defaultValue;
        }
        else return defaultValue;
    }
}
