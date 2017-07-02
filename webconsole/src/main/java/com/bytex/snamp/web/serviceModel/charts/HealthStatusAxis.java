package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Represents axis with health statuses.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("healthStatus")
public final class HealthStatusAxis extends Axis {
    public HealthStatusAxis(){
        setName("Health Statuses");
    }

    /**
     * Gets unit of measurement associated with this axis.
     *
     * @return Unit of measurement associated with this axis.
     */
    @Override
    @JsonIgnore
    public String getUOM() {
        return "threatLevel";
    }
}
