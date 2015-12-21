package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connectors.notifications.NotificationSupport;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

/**
 * Resumes events of the specified managed resource.
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "resume-events",
        description = "Resume all events raised by the managed resource")
public final class ResumeEventsCommand extends EventManagementCommand {
    @Argument(index = 0, name = "resource", required = true, description = "Managed resource to resume")
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
        notifSupport.setSuspended(false);
        return notifSupport.isSuspended() ? "Suspended" : "Not Suspended";
    }
}
