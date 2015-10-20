package com.bytex.snamp.adapters.ssh;

import com.bytex.snamp.adapters.NotificationEventBox;
import com.bytex.snamp.adapters.modeling.NotificationRouter;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.jmx.DescriptorUtils;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.Map;

/**
 * Represents transformation between SNAMP notifications and SSH protocol.
 */
final class SshNotificationAccessor extends NotificationRouter implements SshNotificationMapping {
    static final String LISTEN_COMMAND_PATTERN = "notifs %s";
    final String resourceName;

    SshNotificationAccessor(final MBeanNotificationInfo metadata,
                            final NotificationEventBox mailbox,
                            final String resourceName) {
        super(metadata, mailbox);
        this.resourceName = resourceName;
    }

    @Override
    protected Notification intercept(final Notification notification) {
        notification.setSource(resourceName);
        return notification;
    }

    final String getListenCommand(){
        final Map<String, ?> filterParams = DescriptorUtils.toMap(getDescriptor());
        switch (filterParams.size()) {
            case 0:
                return String.format(LISTEN_COMMAND_PATTERN, "");
            case 1:
                for (final Map.Entry<String, ?> entry : filterParams.entrySet())
                    return String.format(LISTEN_COMMAND_PATTERN, String.format("(%s=%s)", entry.getKey(), entry.getValue()));
            default:
                final StringBuilder filter = new StringBuilder(512);
                for (final Map.Entry<String, ?> entry : filterParams.entrySet())
                    IOUtils.append(filter, "(%s=%s)", entry.getKey(), entry.getValue());
                return String.format("(&(%s))", filter);

        }
    }
}
