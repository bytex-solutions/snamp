package com.bytex.snamp.supervision.elasticity.policies;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractScalingPolicy implements ScalingPolicy {
    double voteWeight;

    AbstractScalingPolicy(final double voteWeight) {
        this.voteWeight = voteWeight;
    }
}
