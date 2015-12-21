package com.bytex.snamp.management.shell;

import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.connectors.notifications.NotificationSupport;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import javax.management.InstanceNotFoundException;

/**
 * Abstract class for all event-related commands.
 */
abstract class EventManagementCommand extends OsgiCommandSupport implements SnampShellCommand {
    protected abstract String getResourceName();

    protected abstract CharSequence doExecute(final NotificationSupport notifSupport);

    @Override
    protected final CharSequence doExecute() throws InstanceNotFoundException {
        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(bundleContext, getResourceName());
        try{
            return doExecute(client.queryObject(NotificationSupport.class));
        }
        finally {
            client.release(bundleContext);
        }
    }
}
