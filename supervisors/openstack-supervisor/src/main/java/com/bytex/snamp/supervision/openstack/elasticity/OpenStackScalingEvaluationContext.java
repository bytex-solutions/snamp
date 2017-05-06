package com.bytex.snamp.supervision.openstack.elasticity;

import com.bytex.snamp.supervision.elasticity.policies.ScalingPolicyEvaluationContext;
import com.google.common.collect.ImmutableMap;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface OpenStackScalingEvaluationContext extends ScalingPolicyEvaluationContext {
    void scaleIn(final double castingVoteWeight, final ImmutableMap<String, Double> policyEvaluation);
    void scaleOut(final double castingVoteWeight, final ImmutableMap<String, Double> policyEvaluation);
}
