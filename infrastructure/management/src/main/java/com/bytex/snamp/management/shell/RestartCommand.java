package com.bytex.snamp.management.shell;

import com.bytex.snamp.management.DefaultSnampManager;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleException;

import java.io.PrintWriter;

/**
 * Restarts SNAMP components.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
@Command(scope = com.bytex.snamp.shell.SnampShellCommand.SCOPE,
    name = "restart",
    description = "Restarts SNAMP components")
@Service
public final class RestartCommand extends SnampShellCommand {

    @Override
    public void execute(final PrintWriter output) throws BundleException {
        DefaultSnampManager.restart(getBundleContext());
    }
}
