package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.gateway.GatewayActivator;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.BundleException;

/**
 * Starts bundle with adapter.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "start-adapter",
    description = "Starts bundle with resource adapter")
public final class StartAdapterCommand extends OsgiCommandSupport implements SnampShellCommand {
    @Argument(index = 0, name = "systemName", required = true, description = "System name of the adapter")
    @SpecialUse
    private String adapterName = "";

    @Override
    protected Void doExecute() throws BundleException {
        GatewayActivator.startResourceAdapter(bundleContext, adapterName);
        return null;
    }
}
