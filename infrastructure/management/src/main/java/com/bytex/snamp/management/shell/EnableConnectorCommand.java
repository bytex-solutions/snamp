package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleException;

/**
 * Starts bundle with resource connector.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "enable-connector",
    description = "Enables bundle with resource connector")
@Service
public final class EnableConnectorCommand extends SnampShellCommand {
    @Argument(name = "systemName", index = 0, required = true, description = "System name of resource connector")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String connectorType = "";

    @Override
    public Void execute() throws BundleException {
        ManagedResourceActivator.enableConnector(getBundleContext(), connectorType);
        return null;
    }
}
