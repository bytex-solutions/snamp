package com.bytex.snamp.management.shell;

import com.bytex.snamp.core.PlatformVersion;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "version",
    description = "Show version of SNAMP platform")
public final class VersionCommand extends OsgiCommandSupport implements SnampShellCommand {
    @Override
    protected CharSequence doExecute() {
        return PlatformVersion.get().toString();
    }
}
