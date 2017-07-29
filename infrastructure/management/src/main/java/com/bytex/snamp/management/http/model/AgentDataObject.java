package com.bytex.snamp.management.http.model;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.AgentConfiguration;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@JsonTypeName("snamp-configuration")
public final class AgentDataObject extends AbstractDataObject<AgentConfiguration> {
    private final Map<String, ResourceDataObject> resources;
    private final Map<String, ResourceGroupDataObject> groups;
    private final Map<String, GatewayDataObject> gateways;
    private final Map<String, ThreadPoolDataObject> threadPools;
    private final Map<String, SupervisorDataObject> supervisors;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public AgentDataObject(){
        resources = new HashMap<>();
        groups = new HashMap<>();
        gateways = new HashMap<>();
        threadPools = new HashMap<>();
        supervisors = new HashMap<>();
    }

    public AgentDataObject(final AgentConfiguration configuration){
        super(configuration);
        resources = Exportable.importEntities(configuration.getResources(), ResourceDataObject::new);
        groups = Exportable.importEntities(configuration.getResourceGroups(), ResourceGroupDataObject::new);
        gateways = Exportable.importEntities(configuration.getGateways(), GatewayDataObject::new);
        threadPools = Exportable.importEntities(configuration.getThreadPools(), ThreadPoolDataObject::new);
        supervisors = Exportable.importEntities(configuration.getSupervisors(), SupervisorDataObject::new);
    }

    /**
     * Exports state of this object into entity configuration.
     *
     * @param entity Entity to modify.
     */
    @Override
    public void exportTo(@Nonnull final AgentConfiguration entity) {
        super.exportTo(entity);
        Exportable.exportEntities(resources, entity.getResources());
        Exportable.exportEntities(groups, entity.getResourceGroups());
        Exportable.exportEntities(gateways, entity.getGateways());
        Exportable.exportEntities(threadPools, entity.getThreadPools());
        Exportable.exportEntities(supervisors, entity.getSupervisors());
    }

    @JsonProperty("threadPools")
    public Map<String, ThreadPoolDataObject> getThreadPools(){
        return threadPools;
    }

    @JsonProperty("resources")
    public Map<String, ResourceDataObject> getResources(){
        return resources;
    }

    @JsonProperty("resourceGroups")
    public Map<String, ResourceGroupDataObject> getResourceGroups(){
        return groups;
    }

    @JsonProperty("gateways")
    public Map<String, GatewayDataObject> getGateways(){
        return gateways;
    }

    @JsonProperty("supervisors")
    public Map<String, SupervisorDataObject> getSupervisors(){
        return supervisors;
    }
}
