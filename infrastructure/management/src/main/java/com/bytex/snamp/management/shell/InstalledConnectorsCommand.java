package com.bytex.snamp.management.shell;

import com.bytex.snamp.management.SnampComponentDescriptor;
import com.bytex.snamp.management.SnampManager;
import com.bytex.snamp.management.jmx.SnampManagerImpl;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import java.io.IOException;

import static com.bytex.snamp.management.shell.Utils.appendln;
import static com.bytex.snamp.management.shell.Utils.getStateString;

/**
 * Prints a list of installed connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "installed-connectors",
        description = "List of installed resource connector")
public final class InstalledConnectorsCommand extends OsgiCommandSupport implements SnampShellCommand {
    private final SnampManager manager = new SnampManagerImpl();

    static void writeConnector(final SnampComponentDescriptor component, final StringBuilder output) {
        appendln(output, "%s. Type: %s. Description: %s. Version: %s. State: %s",
                component.getName(null),
                component.get(SnampComponentDescriptor.CONNECTOR_TYPE_PROPERTY),
                component.toString(null),
                component.getVersion(),
                getStateString(component));
    }

    @Override
    protected CharSequence doExecute() throws IOException {
        final StringBuilder result = new StringBuilder(42);
        for(final SnampComponentDescriptor component: manager.getInstalledResourceConnectors())
            writeConnector(component, result);
        return result;
    }
}
