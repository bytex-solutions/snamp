package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.Aggregator;

import java.util.Map;
import java.util.Set;

/**
 * Represents voting context.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
public interface VotingContext extends Aggregator {
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
