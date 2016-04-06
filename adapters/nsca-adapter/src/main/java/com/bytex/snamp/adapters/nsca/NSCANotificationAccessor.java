package com.bytex.snamp.adapters.nsca;

import com.bytex.snamp.adapters.NotificationListener;
import com.bytex.snamp.adapters.modeling.NotificationRouter;
import com.bytex.snamp.adapters.nsca.configuration.NSCAAdapterConfigurationDescriptor;
import com.bytex.snamp.adapters.nsca.configuration.NSCAAdapterConfigurationParser;
import com.bytex.snamp.concurrent.ThreadSafeObject;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.google.common.collect.ImmutableSet;
import com.googlecode.jsendnsca.core.MessagePayload;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.Set;

import static com.bytex.snamp.adapters.ResourceAdapter.FeatureBindingInfo;

/**
 * Provides transformation between notification of the resource and NSCA protocol.
 */
final class NSCANotificationAccessor extends NotificationRouter implements FeatureBindingInfo<MBeanNotificationInfo> {
    final String resourceName;

    <L extends ThreadSafeObject & NotificationListener> NSCANotificationAccessor(final String resourceName,
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
        return NSCAAdapterConfigurationParser.getServiceName(metadata.getDescriptor(),
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
