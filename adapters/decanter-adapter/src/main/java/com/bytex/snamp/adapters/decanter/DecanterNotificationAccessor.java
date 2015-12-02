package com.bytex.snamp.adapters.decanter;

import com.bytex.snamp.Switch;
import com.bytex.snamp.adapters.modeling.NotificationAccessor;
import com.bytex.snamp.jmx.CompositeDataUtils;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.util.Map;
import java.util.Objects;

/**
 * Represents Decanter-compliant handler of notification supported by the managed resource.
 */
final class DecanterNotificationAccessor extends NotificationAccessor {
    private static final Function<CompositeData, Map<String, ?>> FROM_COMPOSITE_DATA = new Function<CompositeData, Map<String, ?>>() {
        @Override
        public Map<String, ?> apply(final CompositeData input) {
            return CompositeDataUtils.toMap(input);
        }
    };
    private static final Function<ObjectName, String> FROM_OBJECT_NAME = new Function<ObjectName, String>() {
        @Override
        public String apply(final ObjectName input) {
            return input.getCanonicalName();
        }
    };
    private final EventAdmin eventAdmin;
    private final String resourceTopic;

    DecanterNotificationAccessor(final MBeanNotificationInfo metadata,
                                 final EventAdmin admin,
                                 final String topic) {
        super(metadata);
        this.eventAdmin = Objects.requireNonNull(admin);
        this.resourceTopic = Objects.requireNonNull(topic).concat("/");
    }

    private static Object convertUserData(final Notification notification) {
        return new Switch<>()
                .instanceOf(CompositeData.class, FROM_COMPOSITE_DATA)
                .instanceOf(ObjectName.class, FROM_OBJECT_NAME)
                .otherwise(notification.getUserData())
                .apply(notification.getUserData());
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        final ImmutableMap<String, Object> data = ImmutableMap.of(
                "message", notification.getMessage(),
                "sequenceNumber", notification.getSequenceNumber(),
                "timeStamp", notification.getTimeStamp(),
                "data", convertUserData(notification)
        );
        final Event ev = new Event(resourceTopic.concat(notification.getType()), data);
        eventAdmin.postEvent(ev);
    }
}
