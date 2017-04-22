package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.connector.notifications.AccurateNotificationRepository;
import com.bytex.snamp.connector.notifications.NotificationContainer;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.core.SharedCounter;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.management.MBeanException;
import javax.management.Notification;
import javax.management.NotificationListener;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import static com.bytex.snamp.core.SharedObjectType.COUNTER;
import static com.bytex.snamp.core.DistributedServices.getDistributedObject;

/**
 * Represents Groovy-based notification.
 */
final class GroovyNotificationRepository extends AccurateNotificationRepository<GroovyEvent> implements NotificationListener {
    private final Executor listenerInvoker;
    private final SharedCounter sequenceNumberGenerator;
    private final ManagedResourceScriptlet scriptlet;

    GroovyNotificationRepository(final String resourceName,
                                 final ManagedResourceScriptlet scriptlet,
                                 final ExecutorService threadPool,
                                 final BundleContext context) {
        super(resourceName, GroovyEvent.class, true);
        this.scriptlet = Objects.requireNonNull(scriptlet);
        this.listenerInvoker = threadPool;
        this.sequenceNumberGenerator = getDistributedObject(context, "notifications-".concat(resourceName), COUNTER).orElseThrow(AssertionError::new);
        scriptlet.addEventListener(this);
    }

    /**
     * Gets an executor used to execute event listeners.
     *
     * @return Executor service.
     */
    @Override
    @Nonnull
    protected Executor getListenerExecutor() {
        return listenerInvoker;
    }

    @Override
    public Collection<? extends GroovyEvent> expandNotifications() {
        return scriptlet.expandEvents();
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
        notification.setSource(this);
        notification.setSequenceNumber(sequenceNumberGenerator.getAsLong());
        fire(notification.getType(), holder -> ArrayUtils.getFirst(holder.getNotifTypes()).map(newNotifType -> new NotificationContainer(newNotifType, notification)).orElse(null));
    }
}
