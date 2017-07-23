package com.bytex.snamp.management.shell;

import com.bytex.snamp.core.SnampComponentDescriptor;
import com.bytex.snamp.core.SnampManager;
import com.bytex.snamp.management.DefaultSnampManager;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.IOException;
import java.io.PrintWriter;

import static com.bytex.snamp.management.ManagementUtils.getStateString;

/**
 * Prints a list of installed connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = com.bytex.snamp.shell.SnampShellCommand.SCOPE,
        name = "installed-connectors",
        description = "List of installed resource connector")
@Service
public final class InstalledConnectorsCommand extends SnampShellCommand {
    private final SnampManager manager = new DefaultSnampManager();

    static void writeConnector(final SnampComponentDescriptor component, final PrintWriter output) {
        output.format("%s. Type: %s. Description: %s. Version: %s. State: %s",
                component.getName(null),
                component.get(SnampComponentDescriptor.CONNECTOR_TYPE_PROPERTY),
                component.toString(null),
                component.getVersion(),
                getStateString(component)).println();
    }

    @Override
    public void execute(final PrintWriter output) throws IOException {
        for(final SnampComponentDescriptor component: manager.getInstalledResourceConnectors())
            writeConnector(component, output);
    }
}
