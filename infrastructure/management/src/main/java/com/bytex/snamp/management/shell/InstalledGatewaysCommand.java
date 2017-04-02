package com.bytex.snamp.management.shell;

import com.bytex.snamp.core.SnampComponentDescriptor;
import com.bytex.snamp.core.SnampManager;
import com.bytex.snamp.management.DefaultSnampManager;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.io.IOException;

import static com.bytex.snamp.management.ManagementUtils.appendln;
import static com.bytex.snamp.management.ManagementUtils.getStateString;

/**
 * Command that prints list of installed gateway.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "installed-gateways",
        description = "List of installed gateways")
@Service
public final class InstalledGatewaysCommand extends SnampShellCommand  {
    private final SnampManager manager = new DefaultSnampManager();

    static void writeGateway(final SnampComponentDescriptor component, final StringBuilder output) {
        appendln(output, "%s. Name: %s. Description: %s. Version: %s. State: %s",
                component.getName(null),
                component.get(SnampComponentDescriptor.GATEWAY_TYPE_PROPERTY),
                component.toString(null),
                component.getVersion(),
                getStateString(component));
    }

    @Override
    public CharSequence execute() throws IOException {
        final StringBuilder result = new StringBuilder(42);
        for(final SnampComponentDescriptor component: manager.getInstalledGateways())
            writeGateway(component, result);
        return result;
    }
}
