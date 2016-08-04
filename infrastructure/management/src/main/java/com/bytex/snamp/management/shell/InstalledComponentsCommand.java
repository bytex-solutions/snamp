package com.bytex.snamp.management.shell;

import com.bytex.snamp.management.SnampComponentDescriptor;
import com.bytex.snamp.management.SnampManager;
import com.bytex.snamp.management.jmx.SnampManagerImpl;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import java.io.IOException;

import static com.bytex.snamp.management.shell.Utils.appendln;
import static com.bytex.snamp.management.shell.Utils.getStateString;
import static com.bytex.snamp.management.shell.InstalledAdaptersCommand.writeAdapter;
import static com.bytex.snamp.management.shell.InstalledConnectorsCommand.writeConnector;

/**
 * Prints list of installed SNAMP components.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
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
        for(final SnampComponentDescriptor component: manager.getInstalledResourceAdapters())
            writeAdapter(component, result);
        for(final SnampComponentDescriptor component: manager.getInstalledResourceConnectors())
            writeConnector(component, result);
        for(final SnampComponentDescriptor component: manager.getInstalledComponents())
            writeComponent(component, result);
        return result;
    }
}
