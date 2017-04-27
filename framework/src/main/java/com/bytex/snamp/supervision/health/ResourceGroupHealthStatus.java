package com.bytex.snamp.supervision.health;

import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.OkStatus;

import java.util.Map;

/**
 * Represents health status of the managed resource group served by supervisor.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ResourceGroupHealthStatus extends Map<String, HealthStatus> {

    /**
     * Gets accumulated health status of entire group of resources.
     *
     * @return Accumulated health status.
     */
    default HealthStatus getSummaryStatus() {
        return values().stream().reduce(HealthStatus::worst).orElseGet(OkStatus::new);
    }
}
