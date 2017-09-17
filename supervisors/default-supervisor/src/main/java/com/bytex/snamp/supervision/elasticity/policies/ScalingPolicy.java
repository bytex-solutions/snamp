package com.bytex.snamp.supervision.elasticity.policies;

/**
 * Represents a voter in elasticity management process.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.1
 */
public interface ScalingPolicy extends com.bytex.snamp.supervision.elasticity.ScalingPolicy {
    ScalingPolicy VOICELESS = new ScalingPolicy() {
        @Override
        public double evaluate(final ScalingPolicyEvaluationContext context) {
            return 0D;
        }

        @Override
        public void reset() {

        }
    };

    /**
     * Evaluates scaling policy and obtain vote weight.
     * @param context An object containing all necessary data for voting.
     * @return Vote weight: &gt;0 - for scale-out; &lt;0 - for scale-in
     */
    double evaluate(final ScalingPolicyEvaluationContext context);
}
