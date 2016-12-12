package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.gateway.GatewayActivator;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
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
@Service
public final class EnableGatewayCommand extends SnampShellCommand  {
    @Argument(index = 0, name = "gatewayType", required = true, description = "Type of the gateway")
    @SpecialUse
    private String gatewayType = "";

    @Override
    public Void execute() throws BundleException {
        GatewayActivator.enableGateway(getBundleContext(), gatewayType);
        return null;
    }
}
