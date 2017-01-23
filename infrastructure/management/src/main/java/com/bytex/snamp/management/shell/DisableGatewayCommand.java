package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.gateway.GatewayActivator;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
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
@Service
public final class DisableGatewayCommand extends SnampShellCommand {
    @Argument(name = "gatewayType", index = 0, required = true, description = "Type of the gateway")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String gatewayType = "";

    @Override
    public Void execute() throws BundleException {
        GatewayActivator.disableGateway(getBundleContext(), gatewayType);
        return null;
    }
}
