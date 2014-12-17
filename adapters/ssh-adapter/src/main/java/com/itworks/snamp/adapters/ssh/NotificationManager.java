package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.ThreadSafeObject;
import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.connectors.notifications.NotificationFilter;
import org.apache.sshd.common.Session;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents notification manager.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NotificationManager extends ThreadSafeObject implements NotificationFilter {
    private static final Session.AttributeKey<Long> LISTENER_ID  = new Session.AttributeKey<>();
    private static final Session.AttributeKey<NotificationManager> NOTIF_MANAGER = new Session.AttributeKey<>();

    private final Set<String> disabledResources;
    private final Set<String> disabledNotifs;

    private NotificationManager(){
        disabledResources = new HashSet<>();
        disabledNotifs = new HashSet<>();
    }

    Set<String> getDisabledResources(){
        beginRead();
        try{
            return new HashSet<>(disabledResources);
        }
        finally {
            endRead();
        }
    }

    Set<String> getDisabledNotifs(){
        beginRead();
        try{
            return new HashSet<>(disabledNotifs);
        }
        finally {
            endRead();
        }
    }

    boolean isAllowed(final String resourceName, final String eventID) {
        beginRead();
        try {
            return
                    !disabledResources.contains(resourceName) &&
                    !disabledNotifs.contains(eventID);
        } finally {
            endRead();
        }
    }

    void enableNotificationsByResource(final String resourceName){
        beginWrite();
        try{
            disabledResources.remove(resourceName);
        }
        finally {
            endWrite();
        }
    }

    void enableNotificationsByEvent(final String eventName){
        beginWrite();
        try{
            disabledNotifs.remove(eventName);
        }
        finally {
            endWrite();
        }
    }

    void enableAll(){
        beginWrite();
        try{
            disabledNotifs.clear();
            disabledResources.clear();
        }
        finally {
            endWrite();
        }
    }

    void disableAll(final AdapterController controller) {
        beginWrite();
        try {
            disabledNotifs.clear();
            disabledResources.clear();
            for (final String resourceName : controller.getConnectedResources()) {
                disabledResources.add(resourceName);
                disabledNotifs.addAll(controller.getNotifications(resourceName));
            }
        } finally {
            endWrite();
        }
    }

    /**
     * Determines whether the specified notification is allowed for handling.
     *
     * @param resourceName The name of the emitting managed resource.
     * @param eventName    The name of the notification.
     * @param notif        The notification object.
     * @return {@literal true}, if notification is allowed for handling; otherwise, {@literal false}.
     */
    @Override
    public boolean isAllowed(final String resourceName, final String eventName, final Notification notif) {
        return isAllowed(resourceName, eventName);
    }

    static long getNotificationListenerID(final Session s){
        return s.getAttribute(LISTENER_ID);
    }

    static NotificationManager getNotificationManager(final Session s){
        return s.getAttribute(NOTIF_MANAGER);
    }

    static void createNotificationManagerWithDisabledNotifs(final Session s,
                                                            final AdapterController controller){
        final NotificationManager manager = new NotificationManager();
        manager.disableAll(controller);
        s.setAttribute(NOTIF_MANAGER, manager);
    }
}

