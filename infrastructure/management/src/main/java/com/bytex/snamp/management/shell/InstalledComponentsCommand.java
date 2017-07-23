package com.bytex.snamp.management.shell;

import com.bytex.snamp.core.SnampComponentDescriptor;
import com.bytex.snamp.core.SnampManager;
import com.bytex.snamp.management.DefaultSnampManager;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.IOException;
import java.io.PrintWriter;

import static com.bytex.snamp.management.ManagementUtils.getStateString;
import static com.bytex.snamp.management.shell.InstalledConnectorsCommand.writeConnector;
import static com.bytex.snamp.management.shell.InstalledGatewaysCommand.writeGateway;
import static com.bytex.snamp.management.shell.InstalledSupervisorsCommand.writeSupervisor;

/**
 * Prints list of installed SNAMP components.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = com.bytex.snamp.shell.SnampShellCommand.SCOPE,
        name = "installed-components",
        description = "List of installed components")
@Service
public final class InstalledComponentsCommand extends SnampShellCommand  {
    private final SnampManager manager = new DefaultSnampManager();

    private static void writeComponent(final SnampComponentDescriptor component, final PrintWriter output) {
        output.format("%s. Description: %s. Version: %s. State: %s",
                component.getName(null),
                component.toString(null),
                component.getVersion(),
                getStateString(component)).println();
    }

    @Override
    public void execute(final PrintWriter output) throws IOException {
        for (final SnampComponentDescriptor component : manager.getInstalledGateways())
            writeGateway(component, output);
        for (final SnampComponentDescriptor component : manager.getInstalledResourceConnectors())
            writeConnector(component, output);
        for (final SnampComponentDescriptor component : manager.getInstalledComponents())
            writeComponent(component, output);
        for (final SnampComponentDescriptor component : manager.getInstalledSupervisors())
            writeSupervisor(component, output);
    }
}
