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
 * @since 2.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "installed-supervisors",
        description = "List of installed supervisors")
@Service
public final class InstalledSupervisorsCommand extends SnampShellCommand {
    private final SnampManager manager = new DefaultSnampManager();

    static void writeSupervisor(final SnampComponentDescriptor component, final StringBuilder output) {
        appendln(output, "%s. Name: %s. Description: %s. Version: %s. State: %s",
                component.getName(null),
                component.get(SnampComponentDescriptor.SUPERVISOR_TYPE_PROPERTY),
                component.toString(null),
                component.getVersion(),
                getStateString(component));
    }

    @Override
    public CharSequence execute() throws IOException {
        final StringBuilder result = new StringBuilder(42);
        for(final SnampComponentDescriptor component: manager.getInstalledSupervisors())
            writeSupervisor(component, result);
        return result;
    }
}
