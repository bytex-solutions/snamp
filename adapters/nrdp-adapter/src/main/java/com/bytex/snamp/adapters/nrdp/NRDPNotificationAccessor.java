package com.bytex.snamp.adapters.nrdp;

import ch.shamu.jsendnrdp.domain.State;
import com.bytex.snamp.adapters.NotificationListener;
import com.bytex.snamp.adapters.modeling.NotificationRouter;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.google.common.collect.ImmutableSet;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.Set;

import static com.bytex.snamp.adapters.ResourceAdapter.FeatureBindingInfo;
import com.bytex.snamp.adapters.nrdp.configuration.NRDPAdapterConfigurationDescriptor;

/**
 * Provides transformation between notification of the connected resource and NRDP protocol.
 */
final class NRDPNotificationAccessor extends NotificationRouter implements FeatureBindingInfo<MBeanNotificationInfo> {
    final String resourceName;

    <L extends ThreadSafeObject & NotificationListener> NRDPNotificationAccessor(final String resourceName,
                                                                                 final MBeanNotificationInfo metadata,
                                                                                 final L listener) {
        super(metadata, listener);
        this.resourceName = resourceName;
    }

    @Override
    protected Notification intercept(final Notification notification) {
        notification.setSource(resourceName);
        return notification;
    }

    static State getLevel(final MBeanNotificationInfo metadata) {
        switch (NotificationDescriptor.getSeverity(metadata)) {
            case NOTICE:
            case WARNING:
                return State.WARNING;
            case ALERT:
            case ERROR:
            case PANIC:
                return State.CRITICAL;
            case INFO:
            case DEBUG:
            case UNKNOWN:
                return State.OK;
            default:
                return State.UNKNOWN;
        }
    }

    static String getServiceName(final MBeanNotificationInfo metadata) {
        return NRDPAdapterConfigurationDescriptor.getServiceName(metadata.getDescriptor(),
                NotificationDescriptor.getName(metadata));
    }

    @Override
    public Object getProperty(final String propertyName) {
        return null;
    }

    @Override
    public Set<String> getProperties() {
        return ImmutableSet.of();
    }

    @Override
    public boolean setProperty(final String propertyName, final Object value) {
        return false;
    }
}
