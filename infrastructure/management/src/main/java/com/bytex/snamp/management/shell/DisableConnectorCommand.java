package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
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
@Service
public final class DisableConnectorCommand extends SnampShellCommand  {
    @Argument(name = "connectorType", index = 0, required = true, description = "Type of resource connector")
    @SpecialUse
    private String connectorType = "";

    @Override
    public Void execute() throws BundleException {
        ManagedResourceActivator.disableConnector(getBundleContext(), connectorType);
        return null;
    }
}
