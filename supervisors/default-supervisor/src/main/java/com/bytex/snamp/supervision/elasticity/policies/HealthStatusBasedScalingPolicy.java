package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.connector.health.MalfunctionStatus;

import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class HealthStatusBasedScalingPolicy extends AbstractScalingPolicy {
    private final MalfunctionStatus.Level level;

    HealthStatusBasedScalingPolicy(final double voteWeight, final MalfunctionStatus.Level level) {
        super(voteWeight);
        this.level = Objects.requireNonNull(level);
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {

    }



    /**
     * Evaluates scaling policy and obtain vote weight.
     *
     * @param context An object containing all necessary data for voting.
     * @return Vote weight: &gt;0 - for scale-out; &lt;0 - for scale-in
     */
    @Override
    public double evaluate(final ScalingPolicyEvaluationContext context) {
        return 0;
    }
}
