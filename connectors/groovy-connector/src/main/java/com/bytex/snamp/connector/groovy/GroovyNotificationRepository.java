package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.SharedCounter;
import com.bytex.snamp.internal.Utils;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.management.MBeanException;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Represents Groovy-based notification.
 */
final class GroovyNotificationRepository extends AccurateNotificationRepository<GroovyEvent> implements NotificationListener {
    private final ExecutorService listenerInvoker;
    private final SharedCounter sequenceNumberGenerator;
    private final ManagedResourceScriptlet scriptlet;

    GroovyNotificationRepository(final String resourceName,
                                 final ManagedResourceScriptlet scriptlet,
                                 final ExecutorService threadPool) {
        super(resourceName, GroovyEvent.class);
        this.scriptlet = Objects.requireNonNull(scriptlet);
        this.listenerInvoker = threadPool;
        final BundleContext context = Utils.getBundleContextOfObject(this);
        this.sequenceNumberGenerator = ClusterMember.get(context)
                .getCounters()
                .getSharedObject("notifications-".concat(resourceName));
        scriptlet.addEventListener(this);
    }

    /**
     * Gets an executor used to execute event listeners.
     *
     * @return Executor service.
     */
    @Override
    @Nonnull
    protected ExecutorService getListenerExecutor() {
        return listenerInvoker;
    }

    @Override
    public Map<String, NotificationDescriptor> discoverNotifications() {
        final Map<String, NotificationDescriptor> result = new HashMap<>();
        for(final String category: scriptlet.getEvents())
            result.put(category, createDescriptor());
        return result;
    }

    @Override
    protected GroovyEvent connectNotifications(final String notifType, final NotificationDescriptor metadata) throws MBeanException{
        return scriptlet.createEvent(notifType, metadata);
    }

    @Override
    public void close() {
        scriptlet.removeEventListener(this);
        super.close();
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        notification.setSequenceNumber(sequenceNumberGenerator.getAsLong());
        fire(notification, false);
    }
}
