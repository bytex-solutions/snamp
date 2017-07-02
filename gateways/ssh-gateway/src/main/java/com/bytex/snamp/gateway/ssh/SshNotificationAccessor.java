package com.bytex.snamp.gateway.ssh;

import com.bytex.snamp.gateway.NotificationEventBox;
import com.bytex.snamp.gateway.modeling.NotificationRouter;
import com.bytex.snamp.jmx.DescriptorUtils;

import javax.management.MBeanNotificationInfo;
import java.util.Map;

/**
 * Represents transformation between SNAMP notifications and SSH protocol.
 */
final class SshNotificationAccessor extends NotificationRouter implements SshNotificationMapping {
    static final String LISTEN_COMMAND_PATTERN = "notifs %s";

    SshNotificationAccessor(final MBeanNotificationInfo metadata,
                            final NotificationEventBox mailbox,
                            final String resourceName) {
        super(resourceName, metadata, mailbox);
    }

    String getListenCommand(){
        final Map<String, ?> filterParams = DescriptorUtils.toMap(getDescriptor());
        switch (filterParams.size()) {
            case 0:
                return String.format(LISTEN_COMMAND_PATTERN, "");
            case 1:
                return filterParams.entrySet().stream()
                        .map(entry -> String.format(LISTEN_COMMAND_PATTERN, String.format("(%s=%s)", entry.getKey(), entry.getValue())))
                        .findFirst()
                        .orElseGet(() -> null);
            default:
                final StringBuilder filter = new StringBuilder(512);
                for (final Map.Entry<String, ?> entry : filterParams.entrySet())
                    filter.append(String.format("(%s=%s)", entry.getKey(), entry.getValue()));
                return String.format("(&(%s))", filter);

        }
    }
}
