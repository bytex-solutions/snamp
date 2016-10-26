package com.bytex.snamp.management.shell;

import com.bytex.snamp.management.jmx.SnampManagerImpl;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.BundleException;

/**
 * Restarts SNAMP components.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "restart",
    description = "Restarts SNAMP components")
public final class RestartCommand extends OsgiCommandSupport implements SnampShellCommand {

    @Override
    protected Void doExecute() throws BundleException {
        SnampManagerImpl.restart(bundleContext);
        return null;
    }
}
