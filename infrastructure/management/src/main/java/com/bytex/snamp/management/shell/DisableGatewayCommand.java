package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.gateway.GatewayActivator;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.BundleException;

/**
 * Stops bundle with gateway.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "disable-gateway",
    description = "Disables bundle with gateway")
public final class DisableGatewayCommand extends OsgiCommandSupport implements SnampShellCommand {
    @Argument(name = "gatewayType", index = 0, required = true, description = "Type of the gateway")
    @SpecialUse
    private String gatewayType = "";

    @Override
    protected Void doExecute() throws BundleException {
        GatewayActivator.disableGateway(bundleContext, gatewayType);
        return null;
    }
}
