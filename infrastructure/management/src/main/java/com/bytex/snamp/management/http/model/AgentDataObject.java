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
        resources = collectEntities(configuration, ManagedResourceConfiguration.class, ManagedResourceDataObject::new);
        groups = collectEntities(configuration, ManagedResourceGroupConfiguration.class, ResourceGroupDataObject::new);
        gateways = collectEntities(configuration, GatewayConfiguration.class, GatewayDataObject::new);
    }

    private static <F extends EntityConfiguration, DTO extends AbstractDataObject<F>> Map<String, DTO> collectEntities(final AgentConfiguration template,
                                                                                                                       final Class<F> entityType,
                                                                                                                       final Function<? super F, DTO> dataObjectFactory) {
        return collectEntities(template.getEntities(entityType), dataObjectFactory);
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
