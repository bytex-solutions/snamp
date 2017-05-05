package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.Stateful;

/**
 * Represents a voter in elasticity management process.
 */
public interface ScalingPolicy extends Stateful {
    ScalingPolicy VOICELESS = new ScalingPolicy() {
        @Override
        public double evaluate(final ScalingPolicyEvaluationContext context) {
            return 0;
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
