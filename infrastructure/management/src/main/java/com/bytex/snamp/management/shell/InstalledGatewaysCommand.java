package com.bytex.snamp.management.shell;

import com.bytex.snamp.core.SnampComponentDescriptor;
import com.bytex.snamp.core.SnampManager;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.management.DefaultSnampManager;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.IOException;
import java.io.PrintWriter;

import static com.bytex.snamp.management.ManagementUtils.getStateString;

/**
 * Command that prints list of installed gateway.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = Utils.SHELL_COMMAND_SCOPE,
        name = "installed-gateways",
        description = "List of installed gateways")
@Service
public final class InstalledGatewaysCommand extends SnampShellCommand  {
    private final SnampManager manager = new DefaultSnampManager();

    static void writeGateway(final SnampComponentDescriptor component, final PrintWriter output) {
        output.format("%s. Name: %s. Description: %s. Version: %s. State: %s",
                component.getName(null),
                component.get(SnampComponentDescriptor.GATEWAY_TYPE_PROPERTY),
                component.toString(null),
                component.getVersion(),
                getStateString(component)).println();
    }

    @Override
    public void execute(final PrintWriter output) throws IOException {
        for(final SnampComponentDescriptor component: manager.getInstalledGateways())
            writeGateway(component, output);
    }
}
