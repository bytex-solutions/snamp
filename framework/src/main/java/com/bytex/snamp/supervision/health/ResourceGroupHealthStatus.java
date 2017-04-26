package com.bytex.snamp.supervision.health;

import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.OkStatus;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Represents health status of the managed resource group server by supervisor.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class ResourceGroupHealthStatus extends HashMap<String, HealthStatus> implements Serializable {
    private static final long serialVersionUID = 538331150199908998L;

    public ResourceGroupHealthStatus() {

    }

    /**
     * Gets accumulated health status of entire group of resources.
     *
     * @return Accumulated health status.
     */
    public HealthStatus getStatus() {
        return values().stream().reduce(HealthStatus::worst).orElseGet(OkStatus::new);
    }

    @Override
    public String toString() {
        return getStatus().toString();
    }
}
