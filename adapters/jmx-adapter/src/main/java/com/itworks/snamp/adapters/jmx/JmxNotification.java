package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.UserDataSupport;

import javax.management.DynamicMBean;
import javax.management.Notification;

/**
 * Represents wrapped JMX notification.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxNotification extends Notification implements UserDataSupport<Object> {

    public JmxNotification(final String type, final String sourceRes, final Object userData) {
        super(type, sourceRes, 0L);
        setUserData(userData);
    }

    public JmxNotification(final String sender, final com.itworks.snamp.connectors.notifications.Notification notif, final String eventCategory) {
        this(eventCategory, sender, notif.getSeverity().toString());
    }

    /**
     * Gets name of the emitter resource.
     *
     * @return The name of the emitter resource.
     */
    public String getSource() {
        return (String) super.getSource();
    }

    public Notification toWellKnownNotification(final DynamicMBean source, final long sequenceNum) {
        final Notification result = new Notification(getType(), source, sequenceNum, getTimeStamp(), getMessage());
        result.setUserData(getUserData());
        return result;
    }
}
