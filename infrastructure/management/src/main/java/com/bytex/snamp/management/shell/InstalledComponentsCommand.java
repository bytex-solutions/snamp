package com.bytex.snamp.management.shell;

import com.bytex.snamp.core.SnampComponentDescriptor;
import com.bytex.snamp.core.SnampManager;
import com.bytex.snamp.management.jmx.SnampManagerImpl;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import java.io.IOException;

import static com.bytex.snamp.management.shell.Utils.appendln;
import static com.bytex.snamp.management.shell.Utils.getStateString;
import static com.bytex.snamp.management.shell.InstalledGatewaysCommand.writeGateway;
import static com.bytex.snamp.management.shell.InstalledConnectorsCommand.writeConnector;

/**
 * Prints list of installed SNAMP components.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "installed-components",
        description = "List of installed components")
public final class InstalledComponentsCommand extends OsgiCommandSupport implements SnampShellCommand {
    private final SnampManager manager = new SnampManagerImpl();

    private static void writeComponent(final SnampComponentDescriptor component, final StringBuilder output) {
        appendln(output, "%s. Description: %s. Version: %s. State: %s",
                component.getName(null),
                component.toString(null),
                component.getVersion(),
                getStateString(component));
    }

    @Override
    protected CharSequence doExecute() throws IOException{
        final StringBuilder result = new StringBuilder(42);
        for(final SnampComponentDescriptor component: manager.getInstalledGateways())
            writeGateway(component, result);
        for(final SnampComponentDescriptor component: manager.getInstalledResourceConnectors())
            writeConnector(component, result);
        for(final SnampComponentDescriptor component: manager.getInstalledComponents())
            writeComponent(component, result);
        return result;
    }
}
