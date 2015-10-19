package com.bytex.snamp.management.shell;

import com.bytex.snamp.StringAppender;
import com.bytex.snamp.connectors.ManagedResourceActivator;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import java.util.Collection;

import static com.bytex.snamp.internal.Utils.getBundleContextByObject;

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
        final StringAppender result = new StringAppender(50);
        for(final String name: connectors)
            result.appendln(name);
        return result;
    }
}
