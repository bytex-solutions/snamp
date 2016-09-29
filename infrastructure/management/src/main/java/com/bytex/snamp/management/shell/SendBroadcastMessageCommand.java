package com.bytex.snamp.management.shell;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "message",
        description = "Send text message to all members in cluster")
public final class SendBroadcastMessageCommand extends OsgiCommandSupport implements SnampShellCommand {
    @Override
    protected CharSequence doExecute() throws Exception {

        return null;
    }
}
