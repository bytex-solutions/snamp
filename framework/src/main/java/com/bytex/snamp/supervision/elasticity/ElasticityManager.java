package com.bytex.snamp.supervision.elasticity;

import com.bytex.snamp.supervision.SupervisorService;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * Represents elasticity manager for group of resources.
 * @since 2.0
 * @version 2.0
 * @author Roman Sakno
 */
public interface ElasticityManager extends SupervisorService {
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
     * Gets sum of all votes.
     * @return Sum of all votes.
     */
    int getVotes();

    /**
     * Gets weight of votes needed for inflating or shrinking the cluster.
     * @return Weight of votes needed for inflating or shrinking the cluster.
     */
    double getCastingVoteWeight();

    /**
     * Gets current state of the voting process for inflating the cluster.
     * @return The state of the voting process.
     */
    int getUpScalingVotes();

    /**
     * Gets current state of the voting process for shrinking the cluster.
     * @return The state of the voting process.
     */
    int getDownScalingVotes();
}
