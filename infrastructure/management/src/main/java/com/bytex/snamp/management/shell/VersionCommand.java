package com.bytex.snamp.management.shell;

import com.bytex.snamp.core.PlatformVersion;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.PrintWriter;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
@Command(scope = com.bytex.snamp.shell.SnampShellCommand.SCOPE,
    name = "version",
    description = "Show version of SNAMP platform")
@Service
public final class VersionCommand extends SnampShellCommand  {
    @Override
    public void execute(final PrintWriter output) {
        output.print(PlatformVersion.get());
    }
}
