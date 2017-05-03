package com.bytex.snamp.supervision.elasticity;

import com.bytex.snamp.connector.metrics.Rate;
import com.bytex.snamp.supervision.SupervisorAggregatedService;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * Represents elasticity manager for group of resources.
 * @since 2.0
 * @version 2.0
 * @author Roman Sakno
 */
public interface ElasticityManager extends SupervisorAggregatedService {
    /**
     * Gets period that helps to ensure that Elasticity Manager doesn't launch or terminate additional instances before the previous scaling activity takes effect.
     *
     * @return Cooldown period.
     * @see <a href="http://docs.aws.amazon.com/autoscaling/latest/userguide/Cooldown.html">Auto Scaling Cooldowns</a>
     */
    @Nonnull
    Duration getCooldownTime();

    /**
     * Gets number of instances used to enlarge or shrink cluster.
     *
     * @return Scale size.
     */
    int getScale();

    /**
     * Gets statistics about scaling rate.
     * @param action Scaling action.
     * @return Statistics about scaling rate.
     */
    Rate getActionRate(@Nonnull final ScalingAction action);

    /**
     * Gets state of elasticity management process.
     * @return State of elasticity management process.
     */
    @Nonnull
    ElasticityManagementState getState();

    /**
     * Gets weight of votes needed for inflating or shrinking the cluster.
     * @return Weight of votes needed for inflating or shrinking the cluster.
     */
    double getCastingVoteWeight();

    /**
     * Gets result of the last poll.
     * @param subject Voting subject.
     * @return Result of the last poll.
     */
    double getVotes(@Nonnull final ScalingAction subject);
}
