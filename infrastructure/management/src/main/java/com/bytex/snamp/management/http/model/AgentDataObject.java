package com.bytex.snamp.management.http.model;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.*;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("snamp-configuration")
public final class AgentDataObject extends AbstractDataObject<AgentConfiguration> {
    private final Map<String, ManagedResourceDataObject> resources;
    private final Map<String, ResourceGroupDataObject> groups;
    private final Map<String, GatewayDataObject> gateways;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public AgentDataObject(){
        resources = new HashMap<>();
        groups = new HashMap<>();
        gateways = new HashMap<>();
    }

    public AgentDataObject(final AgentConfiguration configuration){
        super(configuration);
        resources = importEntities(configuration, ManagedResourceConfiguration.class, ManagedResourceDataObject::new);
        groups = importEntities(configuration, ManagedResourceGroupConfiguration.class, ResourceGroupDataObject::new);
        gateways = importEntities(configuration, GatewayConfiguration.class, GatewayDataObject::new);
    }

    private static <F extends EntityConfiguration, DTO extends AbstractDataObject<F>> Map<String, DTO> importEntities(final AgentConfiguration template,
                                                                                                                      final Class<F> entityType,
                                                                                                                      final Function<? super F, DTO> dataObjectFactory) {
        return Exportable.importEntities(template.getEntities(entityType), dataObjectFactory);
    }

    private static <F extends EntityConfiguration> void exportEntities(final Map<String, ? extends AbstractDataObject<F>> source,
                                                                       final AgentConfiguration destination,
                                                                       final Class<F> entityType) {
        Exportable.exportEntities(source, destination.getEntities(entityType));
    }

    /**
     * Exports state of this object into entity configuration.
     *
     * @param entity Entity to modify.
     */
    @Override
    public void exportTo(final AgentConfiguration entity) {
        super.exportTo(entity);
        exportEntities(resources, entity, ManagedResourceConfiguration.class);
        exportEntities(groups, entity, ManagedResourceGroupConfiguration.class);
        exportEntities(gateways, entity, GatewayConfiguration.class);
    }

    @JsonProperty("resources")
    public Map<String, ManagedResourceDataObject> getResources(){
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
}
