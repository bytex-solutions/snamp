package com.bytex.snamp.management.shell;

import com.bytex.snamp.management.SnampComponentDescriptor;
import com.bytex.snamp.management.SnampManager;
import com.bytex.snamp.management.jmx.SnampManagerImpl;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import java.io.IOException;

import static com.bytex.snamp.io.IOUtils.appendln;
import static com.bytex.snamp.management.shell.InstalledComponentsCommand.writeComponent;
import static com.bytex.snamp.management.shell.Utils.getStateString;

/**
 * Command that prints list of installed adapters.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "installed-adapters",
        description = "List of installed resource adapters")
public final class InstalledAdaptersCommand extends OsgiCommandSupport implements SnampShellCommand {
    private final SnampManager manager = new SnampManagerImpl();

    static void writeAdapter(final SnampComponentDescriptor component, final StringBuilder output) throws IOException {
        appendln(output, "%s. Name: %s. Description: %s. Version: %s. State: %s",
                component.getName(null),
                component.get(SnampComponentDescriptor.ADAPTER_SYSTEM_NAME_PROPERTY),
                component.getDescription(null),
                component.getVersion(),
                getStateString(component));
    }

    @Override
    protected CharSequence doExecute() throws IOException {
        final StringBuilder result = new StringBuilder(42);
        for(final SnampComponentDescriptor component: manager.getInstalledResourceAdapters())
            writeAdapter(component, result);
        return result;
    }
}
