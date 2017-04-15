package com.bytex.snamp.connector.notifications;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.core.Communicator;
import com.bytex.snamp.core.DistributedServices;
import org.osgi.framework.BundleContext;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents special version of notification repository that can be used by resource connector with unicast model of handling notifications.
 * <p>
 *     This repository can be used by connector if it has the following limitations of handling input notifications:
 *     <ul>
 *         <li>All instances of managed resource connector may accept notifications from resources</li>
 *         <li>It is not possible to avoid duplication of received notification across cluster</li>
 *         <li>All gateways in cluster must be provide the same clone of notification</li>
 *     </ul>
 * @since 2.0
 * @version 2.0
 */
public abstract class AccurateNotificationRepository<M extends MBeanNotificationInfo> extends AbstractNotificationRepository<M> implements Consumer<Communicator.IncomingMessage> {
    private final SafeCloseable subscription;
    private final Consumer<? super Notification> sender;
    private static final String CHANNEL_NAME = "AccurateNotifications";

    /**
     * Initializes a new notification manager.
     *
     * @param resourceName      The name of the managed resource.
     * @param notifMetadataType Type of the notification metadata.
     * @param expandable        {@literal true}, if repository can be populated automatically; otherwise, {@literal false}.
     */
    protected AccurateNotificationRepository(final String resourceName, final Class<M> notifMetadataType, final boolean expandable) {
        super(resourceName, notifMetadataType, expandable);
        final Communicator communicator = DistributedServices.getDistributedCommunicator(getBundleContext(), CHANNEL_NAME);
        subscription = communicator.addMessageListener(this, notificationFilter(resourceName));
        this.sender = communicator::sendSignal;
    }

    private static Predicate<? super Communicator.IncomingMessage> notificationFilter(final String resourceName) {
        return message -> {
            if (message.isRemote() && message.getPayload() instanceof Notification) {
                final Notification n = (Notification) message.getPayload();
                assert n.getSource() instanceof String; //see notificationInterceptor
                return Objects.equals(resourceName, n.getSource());
            } else
                return false;
        };
    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    /**
     * This notification repository is disabled when working in inactive cluster member.
     * @return {@literal true}, if this repository is disabled; otherwise, {@literal false}.
     */
    @Override
    protected final boolean isSuspended() {
        return !DistributedServices.isActiveNode(getBundleContext());
    }

    private void fireListeners(final Notification n) {
        n.setSource(this);
        fireListenersNoIntercept(n);
    }

    @Override
    public final void accept(final Communicator.IncomingMessage message) {
        if (message.getPayload() instanceof Notification)
            fireListeners((Notification) message.getPayload());
    }

    private static Consumer<Notification> notificationInterceptor(final String resourceName,
                                                                  final Consumer<? super Notification> sender){
        return (Notification n) -> {
            n = new NotificationBuilder(n).setSource(resourceName).get();
            sender.accept(n);
        };
    }

    @Override
    protected final void interceptFire(final Collection<? extends Notification> notifications) {
        //send all notifications to other nodes across cluster.
        notifications.forEach(notificationInterceptor(getResourceName(), sender));
    }

    @Override
    public void close() {
        subscription.close();
        super.close();
    }
}
