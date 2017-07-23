package com.bytex.snamp.scripting.debugging;

import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.health.ConnectionProblem;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.shell.SnampShellCommand;
import com.bytex.snamp.supervision.def.DefaultHealthStatusProvider;
import com.bytex.snamp.supervision.health.triggers.HealthStatusTrigger;
import com.bytex.snamp.supervision.health.triggers.TriggerFactory;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * This command used to test health status trigger.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        description = "Executes health status trigger for debugging purposes",
        name = "debug-health-status-trigger")
@Service
public final class DebugHealthStatusTriggerCommand extends ScriptletDebugger<HealthStatusTrigger> implements Action {
    private static final class HealthStatusDebugger extends DefaultHealthStatusProvider {
        void debugGroupStatus(final String groupName, final HealthStatusTrigger trigger) {
            final BundleContext context = Utils.getBundleContextOfObject(this);
            statusBuilder()
                    .updateResourcesStatuses(context, ManagedResourceConnectorClient.selector().setGroupName(groupName).getResources(context))
                    .build(trigger)
                    .close();
        }

        void testStatuses(final HealthStatusTrigger trigger) {
            statusBuilder()
                    .updateResourceStatus("a.com", new OkStatus())
                    .updateResourceStatus("b.com", new OkStatus())
                    .updateResourceStatus("c.com", new ConnectionProblem(new IOException()))
                    .build(trigger)
                    .close();
        }
    }

    @Argument(index = 0, required = true, description = "URL-formatted location of health status trigger written in scripting language")
    private String scriptLocation;

    @Argument(index = 1, required = false, description = "Script language")
    private String language = ScriptletConfiguration.GROOVY_LANGUAGE;

    @Option(name = "-g", aliases = "--group", description = "Name of the real group used to debug trigger. If not specified then test data is used")
    private String groupName;

    @Override
    TriggerFactory createCompiler() {
        return new TriggerFactory();
    }

    @Override
    protected void execute(final PrintWriter writer) throws Exception {
        final HealthStatusTrigger trigger = compile(language, scriptLocation);
        try (final HealthStatusDebugger debugger = new HealthStatusDebugger()) {
            if (groupName != null)
                debugger.debugGroupStatus(groupName, trigger);
            else
                debugger.testStatuses(trigger);
            writer.format("Debugging completed. Health status is %s", debugger.getStatus()).println();
        }
    }
}
