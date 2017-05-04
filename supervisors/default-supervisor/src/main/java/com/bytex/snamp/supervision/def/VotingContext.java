package com.bytex.snamp.supervision.def;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Represents voting context.
 */
public interface VotingContext {
    /**
     * Gets existing resources in the cluster.
     * @return
     */
    Set<String> getResources();
}
