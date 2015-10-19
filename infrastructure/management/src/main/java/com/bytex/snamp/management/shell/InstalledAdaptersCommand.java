package com.bytex.snamp.management.shell;

import com.bytex.snamp.StringAppender;
import com.bytex.snamp.adapters.ResourceAdapterActivator;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import java.util.Collection;

import static com.bytex.snamp.internal.Utils.getBundleContextByObject;

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
        final Collection<String> adapters = ResourceAdapterActivator.getInstalledResourceAdapters(getBundleContextByObject(this));
        final StringAppender result = new StringAppender(50);
        for(final String name: adapters)
            result.appendln(name);
        return result;
    }
}
