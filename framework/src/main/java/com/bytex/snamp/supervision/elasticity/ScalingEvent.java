package com.bytex.snamp.supervision.elasticity;

import com.bytex.snamp.supervision.SupervisionEvent;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Informs about scaling operation.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class ScalingEvent extends SupervisionEvent {
    private static final long serialVersionUID = -3505563874608430847L;

    protected ScalingEvent(@Nonnull final Object source, @Nonnull final String groupName) {
        super(source, groupName);
    }

    /**
     * Gets weight of votes needed for inflating or shrinking the cluster.
     * @return Weight of votes needed for inflating or shrinking the cluster.
     * @see ElasticityManager#getCastingVoteWeight()
     */
    public abstract double getCastingVoteWeight();

    /**
     * Gets snapshot of policy evaluation.
     * @return Snapshot of policy evaluation.
     */
    public abstract Map<String, Double> getPolicyEvaluationResult();
}
