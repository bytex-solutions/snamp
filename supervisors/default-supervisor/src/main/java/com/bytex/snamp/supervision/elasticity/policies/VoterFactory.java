package com.bytex.snamp.supervision.elasticity.policies;

import static com.bytex.snamp.configuration.SupervisorInfo.MetricBasedScalingPolicyInfo;

/**
 * Provides compilation of scaling policies into voters.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class VoterFactory {

    public Voter compile(final String attributeName,
                         final MetricBasedScalingPolicyInfo policyInfo) {
        final MetricBasedVoter voter = new MetricBasedVoter(attributeName, policyInfo.getVoteWeight(), policyInfo.getRange());
        voter.setObservationTime(policyInfo.getObservationTime());
        voter.setIncrementalVoteWeight(policyInfo.isIncrementalVoteWeight());
        voter.setValuesAggregator(policyInfo.getAggregationMethod());
        return voter;
    }
}
