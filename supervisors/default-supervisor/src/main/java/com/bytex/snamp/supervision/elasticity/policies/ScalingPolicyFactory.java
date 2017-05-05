package com.bytex.snamp.supervision.elasticity.policies;

import static com.bytex.snamp.configuration.SupervisorInfo.MetricBasedScalingPolicyInfo;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Provides compilation of scaling policies into voters.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class ScalingPolicyFactory {

    public ScalingPolicy compile(final MetricBasedScalingPolicyInfo policyInfo) {
        if (isNullOrEmpty(policyInfo.getAttributeName()))
            return ScalingPolicy.VOICELESS;
        final MetricBasedScalingPolicy voter = new MetricBasedScalingPolicy(policyInfo.getAttributeName(), policyInfo.getVoteWeight(), policyInfo.getRange());
        voter.setObservationTime(policyInfo.getObservationTime());
        voter.setIncrementalVoteWeight(policyInfo.isIncrementalVoteWeight());
        voter.setValuesAggregator(policyInfo.getAggregationMethod());
        return voter;
    }
}
