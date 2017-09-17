package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;

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
     * Gets all values of the specified attribute in the resource group.
     * @param attributeName Name of the requested attribute.
     * @return Immutable map of attribute values where keys are names of resources in the group.
     */
    Map<String, ?> getAttributes(final String attributeName);

    /**
     * Gets health status of the resource group.
     * @return Health status of the resource group.
     */
    ResourceGroupHealthStatus getHealthStatus();
}
