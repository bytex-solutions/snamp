package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.gateway.GatewayActivator;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.osgi.framework.BundleException;

/**
 * Starts bundle with gateway.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "enable-gateway",
    description = "Enables bundle with gateway")
public final class EnableGatewayCommand extends OsgiCommandSupport implements SnampShellCommand {
    @Argument(index = 0, name = "gatewayType", required = true, description = "Type of the gateway")
    @SpecialUse
    private String gatewayType = "";

    @Override
    protected Void doExecute() throws BundleException {
        GatewayActivator.enableGateway(bundleContext, gatewayType);
        return null;
    }
}
