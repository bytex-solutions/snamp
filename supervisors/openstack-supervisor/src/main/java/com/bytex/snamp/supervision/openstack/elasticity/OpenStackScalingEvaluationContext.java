package com.bytex.snamp.supervision.openstack.elasticity;

import com.bytex.snamp.supervision.elasticity.policies.ScalingPolicyEvaluationContext;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface OpenStackScalingEvaluationContext extends ScalingPolicyEvaluationContext {
    void reportScaleIn(final Map<String, Double> policyEvaluation);
    void reportScaleOut(final Map<String, Double> policyEvaluation);
    void reportMaxClusterSizeReached(final Map<String, Double> policyEvaluation);
}
