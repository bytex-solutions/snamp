package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

/**
 * Suspends events of the specified managed resource.
 */
@Command(scope = SnampShellCommand.SCOPE,
name = "suspend-events",
description = "Suspend all events raised by the managed resource")
public final class SuspendEventsCommand extends EventManagementCommand {
    @Argument(index = 0, name = "resource", required = true, description = "Managed resource to suspend")
    @SpecialUse
    private String resourceName = "";

    @Override
    protected String getResourceName() {
        return resourceName;
    }

    @Override
    protected CharSequence doExecute(final NotificationSupport notifSupport) {
        if(notifSupport == null)
            return "Notifications are not supported";
        notifSupport.setSuspended(true);
        return notifSupport.isSuspended() ? "Suspended" : "Not Suspended";
    }
}
