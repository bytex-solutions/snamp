package com.bytex.snamp.adapters.decanter;

import com.bytex.snamp.adapters.modeling.NotificationAccessor;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.ImmutableMap;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.Objects;

import static com.bytex.snamp.adapters.decanter.DataConverter.convertUserData;

/**
 * Represents Decanter-compliant handler of notification supported by the managed resource.
 */
final class DecanterNotificationAccessor extends NotificationAccessor {
    private final EventAdmin eventAdmin;
    private final String resourceTopic;

    DecanterNotificationAccessor(final MBeanNotificationInfo metadata,
                                 final EventAdmin admin,
                                 final String topic) {
        super(metadata);
        this.eventAdmin = Objects.requireNonNull(admin);
        this.resourceTopic = Objects.requireNonNull(topic).concat("/");
    }

    private static String correctNotificationType(final String notifType){
        return notifType.replace('.', '-');
    }

    private boolean isSuspended(){
        return !DistributedServices.isActiveNode(Utils.getBundleContextOfObject(this));
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        //do not send events in the passive node
        if(isSuspended()) return;

        final ImmutableMap<String, Object> data = ImmutableMap.of(
                "message", notification.getMessage(),
                "sequenceNumber", notification.getSequenceNumber(),
                "severity", NotificationDescriptor.getSeverity(getDescriptor()).toString(),
                "timeStamp", notification.getTimeStamp(),
                "data", convertUserData(notification.getUserData())
        );
        eventAdmin.postEvent(new Event(resourceTopic.concat(correctNotificationType(notification.getType())), data));
    }
}
