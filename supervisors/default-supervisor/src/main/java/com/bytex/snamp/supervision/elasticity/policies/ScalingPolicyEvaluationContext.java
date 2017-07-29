package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.Aggregator;

import java.util.Map;
import java.util.Set;

/**
 * Represents policy evaluation context.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.1
 */
public interface ScalingPolicyEvaluationContext extends Aggregator {
    /**
     * Gets existing resources in the cluster.
     * @return Immutable set of existing resources in the cluster.
     */
    Set<String> getResources();

    /**
     * Gets configuration of the supervisor.
     * @return Configuration of the supervisor.
     */
    Map<String, String> getConfiguration();
}
