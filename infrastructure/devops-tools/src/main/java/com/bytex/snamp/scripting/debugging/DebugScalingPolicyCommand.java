package com.bytex.snamp.scripting.debugging;

import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.shell.SnampShellCommand;
import com.bytex.snamp.supervision.Supervisor;
import com.bytex.snamp.supervision.SupervisorClient;
import com.bytex.snamp.supervision.elasticity.policies.ScalingPolicy;
import com.bytex.snamp.supervision.elasticity.policies.ScalingPolicyEvaluationContext;
import com.bytex.snamp.supervision.elasticity.policies.ScalingPolicyFactory;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        description = "Executes scaling policy for debugging purposes",
        name = "debug-scaling-policy")
@Service
public final class DebugScalingPolicyCommand extends ScriptletDebugger<ScalingPolicy> {
    private static final class ScalingPolicyDebuggerContext implements ScalingPolicyEvaluationContext{
        private final Supervisor supervisor;

        private ScalingPolicyDebuggerContext(final Supervisor supervisor){
            this.supervisor = Objects.requireNonNull(supervisor);
        }

        @Override
        public Set<String> getResources() {
            return supervisor.getResources();
        }

        @Override
        public SupervisorInfo getConfiguration() {
            return supervisor.getConfiguration();
        }

        @Override
        public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
            return supervisor.queryObject(objectType);
        }
    }

    @Argument(index = 0, required = true, description = "URL-formatted location of health status trigger written in scripting language")
    private String scriptLocation;

    @Argument(index = 1, required = false, description = "Script language")
    private String language = ScriptletConfiguration.GROOVY_LANGUAGE;

    @Argument(index = 2, required = true, description = "Name of the resource group")
    private String groupName;
    
    @Override
    ScalingPolicyFactory createCompiler() {
        return new ScalingPolicyFactory();
    }

    @Override
    protected void execute(final PrintWriter writer) throws Exception {
        final ScalingPolicy policy = compile(language, scriptLocation);
        final BundleContext context = Utils.getBundleContextOfObject(this);
        final Optional<SupervisorClient> supervisorRef = SupervisorClient.tryCreate(context, groupName);
        if (supervisorRef.isPresent()) {
            try (final SupervisorClient client = supervisorRef.get()) {
                final double weight = policy.evaluate(new ScalingPolicyDebuggerContext(client));
                writer.format("Scaling policy executed successfully. Evaluation result is %s", weight).println();
            }
        } else
            writer.format("Resource group %s doesn't exist", groupName).println();
    }
}
