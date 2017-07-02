package com.bytex.snamp.connector.dataStream.groovy;

import groovy.lang.GroovySystem;
import groovy.lang.Script;

import javax.management.Notification;
import javax.management.NotificationFilter;

import static org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation.castToBoolean;

/**
 * Represents Groovy-based notification filter.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class GroovyNotificationFilter extends Script implements NotificationFilter {
    private static final long serialVersionUID = 3588476858948141839L;
    private final ThreadLocal<Notification> notificationStorage;

    protected GroovyNotificationFilter(){
        notificationStorage = new ThreadLocal<>();
    }

    @Override
    public final Object getProperty(final String property) {
        final Notification n = notificationStorage.get();
        return n == null ? super.getProperty(property) : GroovySystem.getMetaClassRegistry()
                .getMetaClass(n.getClass())
                .getProperty(getClass(), n, property, false, false);
    }

    /**
     * Invoked before sending the specified notification to the listener.
     *
     * @param notification The notification to be sent.
     * @return <CODE>true</CODE> if the notification has to be sent to the listener, <CODE>false</CODE> otherwise.
     */
    @Override
    public final boolean isNotificationEnabled(final Notification notification) {
        notificationStorage.set(notification);
        final Object result;
        try {
            result = run();
        } finally {
            notificationStorage.remove();
        }
        return castToBoolean(result);
    }
}
