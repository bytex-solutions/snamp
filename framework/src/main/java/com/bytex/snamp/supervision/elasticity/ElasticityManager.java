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
     * Gets number of active instances running at this time.
     * @return Number of active instances running at this time.
     */
    int getInstances();
}
