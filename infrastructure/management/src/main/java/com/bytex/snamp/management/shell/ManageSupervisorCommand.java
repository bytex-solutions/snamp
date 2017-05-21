package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.supervision.SupervisorActivator;
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
 * @since 2.0
 */
@Command(scope = Utils.SHELL_COMMAND_SCOPE,
        name = "manage-supervisor",
        description = "Enables bundle with supervisor")
@Service
public class ManageSupervisorCommand extends SnampShellCommand {
    @Argument(index = 0, name = "supervisorType", required = true, description = "Type of the supervisor")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String supervisorType = "";

    @Option(name = "-e", aliases = {"--enable"}, required = true, description = "Enable or disable supervisor")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private boolean enable = false;

    @Override
    public void execute(final PrintWriter output) throws BundleException {
        if (enable)
            SupervisorActivator.enableSupervisor(getBundleContext(), supervisorType);
        else
            SupervisorActivator.disableSupervsior(getBundleContext(), supervisorType);
    }
}
