package com.bytex.snamp.supervision.elasticity;

/**
 * Represents scaling policy with the default vote weight.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface WeightedScalingPolicy extends ScalingPolicy {
    /**
     * Gets vote weight of the scaling policy.
     * @return Vote weight of the scaling policy.
     */
    double getVoteWeight();
}
