package com.bytex.snamp.scripting.debugging;

import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.shell.SnampShellCommand;
import com.bytex.snamp.supervision.SupervisorClient;
import com.bytex.snamp.supervision.elasticity.policies.ScalingPolicy;
import com.bytex.snamp.supervision.elasticity.policies.ScalingPolicyEvaluationContext;
import com.bytex.snamp.supervision.elasticity.policies.ScalingPolicyFactory;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.management.JMException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@Command(scope = SnampShellCommand.SCOPE,
        description = "Executes scaling policy for debugging purposes",
        name = "debug-scaling-policy")
@Service
public final class DebugScalingPolicyCommand extends ScriptletDebugger<ScalingPolicy> {
    private static final class ScalingPolicyDebuggerContext implements ScalingPolicyEvaluationContext{
        private final SupervisorClient supervisor;
        private final PrintWriter logger;
        private final BundleContext context;

        private ScalingPolicyDebuggerContext(final SupervisorClient supervisor,
                                             final PrintWriter writer,
                                             final BundleContext context){
            this.supervisor = Objects.requireNonNull(supervisor);
            this.logger = Objects.requireNonNull(writer);
            this.context = Objects.requireNonNull(context);
        }

        @Override
        public Set<String> getResources() {
            return supervisor.getResources();
        }

        /**
         * Gets all values of the specified attribute in the resource group.
         *
         * @param attributeName Name of the requested attribute.
         * @return Immutable map of attribute values where keys are names of resources in the group.
         */
        @Override
        public Map<String, ?> getAttributes(final String attributeName) {
            final Map<String, Object> attributes = new HashMap<>();
            for (final String resourceName : getResources()) {
                final Optional<ManagedResourceConnectorClient> clientRef = ManagedResourceConnectorClient.tryCreate(context, resourceName);
                if (clientRef.isPresent())
                    try (final ManagedResourceConnectorClient client = clientRef.get()) {
                        attributes.put(client.getManagedResourceName(), client.getAttribute(attributeName));
                    } catch (final JMException e) {
                        logger.format("Attribute %s cannot obtained. Reason: %s", attributeName, e).println();
                    }
            }
            return attributes;
        }

        /**
         * Gets health status of the resource group.
         *
         * @return Health status of the resource group.
         */
        @Override
        public ResourceGroupHealthStatus getHealthStatus() {
            return supervisor.getHealthStatus();
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
                final double weight = policy.evaluate(new ScalingPolicyDebuggerContext(client, writer, context));
                writer.format("Scaling policy executed successfully. Evaluation result is %s", weight).println();
            }
        } else
            writer.format("Resource group %s doesn't exist", groupName).println();
    }
}
