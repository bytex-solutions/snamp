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
 * @since 2.0
 */
@Command(scope = Utils.SHELL_COMMAND_SCOPE,
        name = "installed-supervisors",
        description = "List of installed supervisors")
@Service
public final class InstalledSupervisorsCommand extends SnampShellCommand {
    private final SnampManager manager = new DefaultSnampManager();

    static void writeSupervisor(final SnampComponentDescriptor component, final PrintWriter output) {
        output.format("%s. Name: %s. Description: %s. Version: %s. State: %s",
                component.getName(null),
                component.get(SnampComponentDescriptor.SUPERVISOR_TYPE_PROPERTY),
                component.toString(null),
                component.getVersion(),
                getStateString(component)).println();
    }

    @Override
    public void execute(final PrintWriter output) throws IOException {
        for (final SnampComponentDescriptor component : manager.getInstalledSupervisors())
            writeSupervisor(component, output);
    }
}
