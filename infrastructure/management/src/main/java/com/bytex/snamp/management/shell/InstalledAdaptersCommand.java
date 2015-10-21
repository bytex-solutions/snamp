package com.bytex.snamp.management.shell;

import com.bytex.snamp.adapters.ResourceAdapterActivator;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import java.util.Collection;

import static com.bytex.snamp.io.IOUtils.appendln;

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

    @Override
    protected CharSequence doExecute() {
        final Collection<String> adapters = ResourceAdapterActivator.getInstalledResourceAdapters(bundleContext);
        final StringBuilder result = new StringBuilder(50);
        for (final String name : adapters)
            appendln(result, name);
        return result;
    }
}
