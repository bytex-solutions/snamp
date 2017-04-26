package com.bytex.snamp.web.serviceModel.resourceGroups;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.health.HealthCheckSupport;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.supervision.Supervisor;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.HashMap;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonSerialize(contentUsing = HealthStatusSerializer.class)
public final class HealthStatusMap extends HashMap<String, HealthStatus> {
    private static final long serialVersionUID = 2645921325913575632L;

    HealthStatusMap(){
        
    }

    @JsonIgnore
    HealthStatus getWorstStatus(){
        return values().stream().reduce(HealthStatus::worst).orElseGet(OkStatus::new);
    }

    private void putStatus(final String key, final Aggregator aggregator){
        final HealthStatus status = aggregator.queryObject(HealthCheckSupport.class)
                .map(HealthCheckSupport::getStatus)
                .orElseGet(OkStatus::new);
        put(key, status);
    }

    void putStatus(final String groupName, final Supervisor supervisor) {
        putStatus(groupName, (Aggregator) supervisor);
    }

    void putStatus(final String resourceName, final ManagedResourceConnector connector) {
        putStatus(resourceName, (Aggregator) connector);
    }
}
