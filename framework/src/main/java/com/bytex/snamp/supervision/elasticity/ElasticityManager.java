package com.bytex.snamp.supervision.elasticity;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.connector.metrics.MetricsSupport;
import com.bytex.snamp.supervision.SupervisorAggregatedService;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * Represents elasticity manager for group of resources.
 * @since 2.0
 * @version 2.0
 * @author Roman Sakno
 */
public interface ElasticityManager extends SupervisorAggregatedService, Stateful, MetricsSupport {

    /**
     * Gets period that helps to ensure that Elasticity Manager doesn't launch or terminate additional instances before the previous scaling activity takes effect.
     *
     * @return Cooldown period.
     * @see <a href="http://docs.aws.amazon.com/autoscaling/latest/userguide/Cooldown.html">Auto Scaling Cooldowns</a>
     */
    @Nonnull
    Duration getCooldownTime();

    /**
     * Gets maximum number of resources in cluster.
     * @return Maximum number of resources in cluster.
     * @implNote {@link Integer#MAX_VALUE} means that there is no upper bound for the cluster size.
     */
    int getMaxClusterSize();

    /**
     * Gets minimum number of resources in cluster.
     * @return Minimum number of resources in cluster.
     */
    int getMinClusterSize();

    /**
     * Gets number of instances used to enlarge or shrink cluster.
     *
     * @return Scale size.
     */
    int getScalingSize();

    double getVotesForScaleIn();

    double getVotesForScaleOut();

    /**
     * Gets weight of votes needed for inflating or shrinking the cluster.
     * @return Weight of votes needed for inflating or shrinking the cluster.
     */
    double getCastingVoteWeight();

    /**
     * Gets scaling metrics.
     * @return Scaling metrics.
     */
    ScalingMetrics getScalingMetrics();
}
