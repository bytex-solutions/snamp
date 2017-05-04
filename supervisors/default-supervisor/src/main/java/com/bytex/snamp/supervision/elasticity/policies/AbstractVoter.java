package com.bytex.snamp.supervision.elasticity.policies;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractVoter implements Voter {
    final double voteWeight;

    AbstractVoter(final double voteWeight) {
        this.voteWeight = voteWeight;
    }
}
