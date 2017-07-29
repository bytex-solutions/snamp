package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleException;

import java.io.PrintWriter;

/**
 * Starts bundle with resource connector.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
@Command(scope = com.bytex.snamp.shell.SnampShellCommand.SCOPE,
    name = "manage-connector",
    description = "Enables bundle with resource connector")
@Service
public final class ManageConnectorCommand extends SnampShellCommand {
    @Argument(name = "systemName", index = 0, required = true, description = "System name of resource connector")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String connectorType = "";

    @Option(name = "-e", aliases = {"--enable"}, required = true, description = "Enable or disable connector")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private boolean enable = false;

    @Override
    public void execute(final PrintWriter output) throws BundleException {
        if (enable)
            ManagedResourceActivator.enableConnector(getBundleContext(), connectorType);
        else
            ManagedResourceActivator.disableConnector(getBundleContext(), connectorType);
    }
}
