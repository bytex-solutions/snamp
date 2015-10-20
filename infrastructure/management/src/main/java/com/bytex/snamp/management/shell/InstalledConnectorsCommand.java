package com.bytex.snamp.management.shell;

import com.bytex.snamp.connectors.ManagedResourceActivator;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import java.util.Collection;

import static com.bytex.snamp.internal.Utils.getBundleContextByObject;
import static com.bytex.snamp.io.IOUtils.appendln;

/**
 * Prints a list of installed connectors.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "installed-connectors",
        description = "List of installed resource connectors")
public final class InstalledConnectorsCommand extends OsgiCommandSupport implements SnampShellCommand {
    @Override
    protected CharSequence doExecute() {
        final Collection<String> connectors = ManagedResourceActivator.getInstalledResourceConnectors(getBundleContextByObject(this));
        final StringBuilder result = new StringBuilder(50);
        for (final String name : connectors)
            appendln(result, name);
        return result;
    }
}
