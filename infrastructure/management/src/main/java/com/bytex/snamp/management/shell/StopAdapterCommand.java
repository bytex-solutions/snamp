package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.adapters.ResourceAdapterActivator;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.BundleException;

/**
 * Stops bundle with resource adapter.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "stop-adapter",
    description = "Stops bundle with resource adapter")
public final class StopAdapterCommand extends OsgiCommandSupport implements SnampShellCommand {
    @Argument(name = "systemName", index = 0, required = true, description = "System name of the adapter")
    @SpecialUse
    private String adapterName = "";

    @Override
    protected Void doExecute() throws BundleException {
        ResourceAdapterActivator.stopResourceAdapter(bundleContext, adapterName);
        return null;
    }
}
