package com.bytex.snamp.web.serviceModel.resourceGroups;

import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.supervision.Supervisor;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Represents resource group status.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ResourceGroupStatus {
    private final HealthStatusMap resources;
    private final HealthStatus summary;

    ResourceGroupStatus(final Supervisor supervisor){
        resources = new HealthStatusMap();
        summary = null;
    }

    @JsonProperty("summary")
    @JsonSerialize(using = HealthStatusSerializer.class)
    public HealthStatus getSummary(){
        final HealthStatus worst = resources.getWorstStatus();
        return summary;
    }
}
