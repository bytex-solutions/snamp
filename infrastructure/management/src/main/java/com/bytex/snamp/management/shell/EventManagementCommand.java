package com.bytex.snamp.management.shell;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.notifications.NotificationSupport;

import javax.management.InstanceNotFoundException;

/**
 * Abstract class for all event-related commands.
 */
abstract class EventManagementCommand extends SnampShellCommand  {
    protected abstract String getResourceName();

    protected abstract CharSequence doExecute(final NotificationSupport notifSupport);

    @Override
    public final CharSequence execute() throws InstanceNotFoundException {
        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(getBundleContext(), getResourceName());
        try{
            return doExecute(client.queryObject(NotificationSupport.class));
        }
        finally {
            client.release(getBundleContext());
        }
    }
}
