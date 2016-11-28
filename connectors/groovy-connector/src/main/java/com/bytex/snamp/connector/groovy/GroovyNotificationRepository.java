package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.MethodStub;
import com.bytex.snamp.connector.notifications.*;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.LongCounter;
import org.osgi.framework.BundleContext;

import javax.management.MBeanException;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents Groovy-based notification.
 */
final class GroovyNotificationRepository extends AccurateNotificationRepository<GroovyEvent> implements NotificationListener {
    private final NotificationListenerInvoker listenerInvoker;
    private final LongCounter sequenceNumberGenerator;
    private final ManagedResourceScriptlet scriptlet;

    GroovyNotificationRepository(final String resourceName,
                                 final ManagedResourceScriptlet scriptlet,
                                 final ExecutorService threadPool,
                                 final BundleContext context) {
        super(resourceName, GroovyEvent.class, true);
        this.scriptlet = Objects.requireNonNull(scriptlet);
        this.listenerInvoker = createListenerInvoker(threadPool, scriptlet.getLogger());
        this.sequenceNumberGenerator = DistributedServices.getDistributedCounter(context, "notifications-".concat(resourceName));
        scriptlet.addEventListener(this);
    }

    @Override
    public Collection<? extends GroovyEvent> expandNotifications() {
        return scriptlet.expandEvents();
    }

    private static NotificationListenerInvoker createListenerInvoker(final Executor executor, final Logger logger) {
        return NotificationListenerInvokerFactory.createParallelExceptionResistantInvoker(executor, (e, source) -> logger.log(Level.SEVERE, "Unable to process JMX notification.", e));
    }

    /**
     * Gets the invoker used to executed notification listeners.
     *
     * @return The notification listener invoker.
     */
    @Override
    protected NotificationListenerInvoker getListenerInvoker() {
        return listenerInvoker;
    }

    @Override
    protected GroovyEvent connectNotifications(final String notifType, final NotificationDescriptor metadata) throws MBeanException{
        return scriptlet.createEvent(notifType, metadata);
    }

    /**
     * Reports an error when enabling notifications.
     *
     * @param category An event category.
     * @param e        Internal connector error.
     * @see #failedToEnableNotifications(Logger, Level, String, Exception)
     */
    @Override
    protected void failedToEnableNotifications(final String category, final Exception e) {
        failedToEnableNotifications(scriptlet.getLogger(), Level.SEVERE, category, e);
    }

    @Override
    public void close() {
        scriptlet.removeEventListener(this);
        super.close();
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        notification.setSource(this);
        notification.setSequenceNumber(sequenceNumberGenerator.getAsLong());
        fire(notification.getType(), holder -> new NotificationContainer(holder.getName(), notification));
    }
}
