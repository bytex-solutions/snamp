package com.bytex.snamp.supervision.health;

import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.OkStatus;

import java.util.Map;
import java.util.Optional;

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

    static Optional<String> getMostProblematicResource(final Map<String, HealthStatus> resources) {
        String resourceName = null;
        HealthStatus worstStatus = null;
        for (final Entry<String, HealthStatus> entry : resources.entrySet())
            if (OkStatus.notOk(entry.getValue()))
                if (worstStatus == null || entry.getValue().worseThan(worstStatus)) {
                    worstStatus = entry.getValue();
                    resourceName = entry.getKey();
                }
        return Optional.ofNullable(resourceName);
    }
}
