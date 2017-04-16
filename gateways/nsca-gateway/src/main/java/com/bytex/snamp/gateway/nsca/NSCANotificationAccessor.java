package com.bytex.snamp.gateway.nsca;

import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.modeling.NotificationRouter;
import com.google.common.collect.ImmutableSet;
import com.googlecode.jsendnsca.core.MessagePayload;

import javax.management.MBeanNotificationInfo;
import java.util.Set;

import static com.bytex.snamp.gateway.Gateway.FeatureBindingInfo;

/**
 * Provides transformation between notification of the resource and NSCA protocol.
 */
final class NSCANotificationAccessor extends NotificationRouter implements FeatureBindingInfo<MBeanNotificationInfo> {
    <L extends ThreadSafeObject & NotificationListener> NSCANotificationAccessor(final String resourceName,
                                                                                 final MBeanNotificationInfo metadata,
                                                                                 final L listener) {
        super(resourceName, metadata, listener);
    }

    static int getLevel(final MBeanNotificationInfo metadata) {
        switch (NotificationDescriptor.getSeverity(metadata)) {
            case ALERT:
            case WARNING:
                return MessagePayload.LEVEL_WARNING;
            case ERROR:
            case PANIC:
                return MessagePayload.LEVEL_CRITICAL;
            case NOTICE:
            case INFO:
            case DEBUG:
            case UNKNOWN:
                return MessagePayload.LEVEL_OK;
            default:
                return MessagePayload.LEVEL_UNKNOWN;
        }
    }

    static String getServiceName(final MBeanNotificationInfo metadata) {
        return NSCAGatewayConfigurationDescriptor.getServiceName(metadata.getDescriptor(),
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
