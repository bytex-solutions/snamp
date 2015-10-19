package com.bytex.snamp.management.shell;

import com.bytex.snamp.StringAppender;
import com.bytex.snamp.management.SnampComponentDescriptor;
import com.bytex.snamp.management.SnampManager;
import com.bytex.snamp.management.jmx.SnampManagerImpl;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import java.io.IOException;

/**
 * Prints list of installed SNAMP components.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "installed-components",
        description = "List of installed components")
public final class InstalledComponentsCommand extends OsgiCommandSupport implements SnampShellCommand {
    private final SnampManager manager = new SnampManagerImpl();

    private static void writeComponent(final SnampComponentDescriptor component, final StringAppender output) throws IOException {
        output.appendln("Name: %s. Description: %s. Version: %s. State: %s",
                component.getName(null),
                component.getDescription(null),
                component.getVersion(),
                component.getState());
    }

    @Override
    protected CharSequence doExecute() throws IOException{
        final StringAppender result = new StringAppender(42);
        for(final SnampComponentDescriptor component: manager.getInstalledResourceAdapters())
            writeComponent(component, result);
        for(final SnampComponentDescriptor component: manager.getInstalledResourceConnectors())
            writeComponent(component, result);
        for(final SnampComponentDescriptor component: manager.getInstalledComponents())
            writeComponent(component, result);
        return result;
    }
}
