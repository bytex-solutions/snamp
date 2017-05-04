package com.bytex.snamp.supervision.elasticity.policies;

import static com.bytex.snamp.configuration.SupervisorInfo.ScalingPolicyInfo;
import static com.bytex.snamp.configuration.SupervisorInfo.MetricBasedScalingPolicyInfo;

/**
 * Provides compilation of scaling policies into voters.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class VoterFactory {

    public Voter compile(final ScalingPolicyInfo policyInfo) throws VoterCompilationException{
        if(policyInfo instanceof MetricBasedScalingPolicyInfo){
            
        }
        return null;
    }
}
