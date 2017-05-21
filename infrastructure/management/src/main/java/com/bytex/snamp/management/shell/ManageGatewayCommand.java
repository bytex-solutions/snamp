package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.internal.Utils;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleException;

import java.io.PrintWriter;

/**
 * Starts bundle with gateway.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = Utils.SHELL_COMMAND_SCOPE,
    name = "manage-gateway",
    description = "Enables bundle with gateway")
@Service
public final class ManageGatewayCommand extends SnampShellCommand  {
    @Argument(index = 0, name = "gatewayType", required = true, description = "Type of the gateway")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String gatewayType = "";

    @Option(name = "-e", aliases = {"--enable"}, required = true, description = "Enable or disable gateway")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private boolean enable = false;

    @Override
    public void execute(final PrintWriter output) throws BundleException {
        if (enable)
            GatewayActivator.enableGateway(getBundleContext(), gatewayType);
        else
            GatewayActivator.disableGateway(getBundleContext(), gatewayType);
    }
}
