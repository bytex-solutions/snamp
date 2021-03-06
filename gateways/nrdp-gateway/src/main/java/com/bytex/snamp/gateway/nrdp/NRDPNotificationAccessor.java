package com.bytex.snamp.gateway.nrdp;

import ch.shamu.jsendnrdp.domain.State;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.modeling.NotificationRouter;
import com.google.common.collect.ImmutableSet;

import javax.management.MBeanNotificationInfo;
import java.util.Set;

import static com.bytex.snamp.gateway.Gateway.FeatureBindingInfo;

/**
 * Provides transformation between notification of the connected resource and NRDP protocol.
 */
final class NRDPNotificationAccessor extends NotificationRouter implements FeatureBindingInfo<MBeanNotificationInfo> {
    NRDPNotificationAccessor(final String resourceName,
                                                                                 final MBeanNotificationInfo metadata,
                                                                                 final NotificationListener listener) {
        super(resourceName, metadata, listener);
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
        return NRDPGatewayConfigurationDescriptor.getServiceName(metadata.getDescriptor(),
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
