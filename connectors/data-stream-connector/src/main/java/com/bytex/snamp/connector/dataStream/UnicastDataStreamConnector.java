package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.notifications.NotificationExchange;
import com.bytex.snamp.core.ClusterMember;
import org.osgi.framework.BundleContext;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.management.Notification;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents abstract class for stream-driven resources connector which accepts notifications only at single SNAMP
 * node and distribute this notification across cluster nodes.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see com.bytex.snamp.connector.notifications.AccurateNotificationRepository
 */
public abstract class UnicastDataStreamConnector extends DataStreamConnector {
    private static final String CHANNEL_NAME = "UnicastDataStream";
    private final NotificationExchange exchange;
    private final ClusterMember clusterMember;

    protected UnicastDataStreamConnector(final String resourceName, final ManagedResourceInfo configuration, final DataStreamConnectorConfigurationDescriptionProvider descriptor) {
        super(resourceName, configuration, descriptor);
        clusterMember = ClusterMember.get(getBundleContext());
        exchange = NotificationExchange.create(clusterMember, CHANNEL_NAME, resourceName, this::acceptRaw);
    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    protected final void accept(final Notification notification, final boolean broadcast){
        super.accept(notification);
        if (broadcast && clusterMember.isActive()) {
            notification.setSource(attributes.getResourceName());
            exchange.send(notification);
        }
    }

    /**
     * Invoked when a JMX notification occurs.
     * The implementation of this method should return as soon as possible, to avoid
     * blocking its notification broadcaster.
     *
     * @param notification The notification.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void accept(final Notification notification) {
        accept(notification, true);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resource clearly.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() throws Exception {
        super.close();
        exchange.close();
    }
}
