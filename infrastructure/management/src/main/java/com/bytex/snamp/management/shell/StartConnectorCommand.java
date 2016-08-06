package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connectors.ManagedResourceActivator;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.BundleException;

/**
 * Starts bundle with resource connector.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "start-connector",
    description = "Starts bundle with resource connector")
public final class StartConnectorCommand extends OsgiCommandSupport implements SnampShellCommand {
    @Argument(name = "systemName", index = 0, required = true, description = "System name of resource connector")
    @SpecialUse
    private String connectorName = "";

    @Override
    protected Void doExecute() throws BundleException {
        ManagedResourceActivator.startResourceConnector(bundleContext, connectorName);
        return null;
    }
}
