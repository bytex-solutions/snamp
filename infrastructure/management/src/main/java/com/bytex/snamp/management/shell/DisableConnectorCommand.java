package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.BundleException;

/**
 * Stops bundle with resource connector.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "disable-connector",
    description = "Disables bundle with resource connector")
public final class DisableConnectorCommand extends OsgiCommandSupport implements SnampShellCommand {
    @Argument(name = "connectorType", index = 0, required = true, description = "Type of resource connector")
    @SpecialUse
    private String connectorType = "";

    @Override
    protected Void doExecute() throws BundleException {
        ManagedResourceActivator.disableConnector(bundleContext, connectorType);
        return null;
    }
}
