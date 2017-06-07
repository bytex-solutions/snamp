package com.bytex.snamp.supervision.elasticity;

import com.bytex.snamp.connector.health.MalfunctionStatus;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;

/**
 * Represents scaling policy based on heath status of resource group.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface HealthStatusBasedScalingPolicy extends WeightedScalingPolicy {
    /**
     * Gets level of malfunction used as voting trigger.
     * @return Level of malfunction.
     * @implSpec If {@link ResourceGroupHealthStatus#getSummaryStatus()} is greater or equal to this level then scaling policy will return non-zero weight.
     */
    MalfunctionStatus.Level getLevel();
}
