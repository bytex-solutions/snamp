package com.bytex.snamp.connector.notifications;

import com.bytex.snamp.Convert;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.Communicator;
import org.osgi.framework.BundleContext;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.Collection;
import java.util.Objects;
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
public abstract class AccurateNotificationRepository<M extends MBeanNotificationInfo> extends AbstractNotificationRepository<M> {
    private final NotificationExchange exchange;
    private final ClusterMember clusterMember;
    private static final String CHANNEL_NAME = "AccurateNotifications";

    /**
     * Initializes a new notification manager.
     *
     * @param resourceName      The name of the managed resource.
     * @param notifMetadataType Type of the notification metadata.
     */
    protected AccurateNotificationRepository(final String resourceName, final Class<M> notifMetadataType) {
        super(resourceName, notifMetadataType);
        clusterMember = ClusterMember.get(getBundleContext());
        exchange = NotificationExchange.create(clusterMember, CHANNEL_NAME, resourceName, this::fireListenersNoIntercept);
    }

    private static Predicate<? super Communicator.MessageEvent> notificationFilter(final String resourceName) {
        return message -> message.isRemote() &&
                Convert.toType(message.getPayload(), Notification.class)
                        .filter(payload -> Objects.equals(resourceName, payload.getSource()))
                        .isPresent();
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
        return !clusterMember.isActive();
    }

    @Override
    protected final void interceptFire(final Collection<? extends Notification> notifications) {
        //send all notifications to other nodes across cluster.
        for (final Notification n : notifications) {
            n.setSource(getResourceName());
            exchange.send(n);
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() {
        super.close();
        exchange.close();
    }
}
