package com.bytex.snamp.management.http.model;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityConfiguration;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * AbstractDTOEntity
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@JsonSubTypes({
        @JsonSubTypes.Type(EventDataObject.class),
        @JsonSubTypes.Type(OperationDataObject.class),
        @JsonSubTypes.Type(AttributeDataObject.class),
        @JsonSubTypes.Type(ResourceDataObject.class),
        @JsonSubTypes.Type(GatewayDataObject.class),
        @JsonSubTypes.Type(ResourceGroupDataObject.class),
        @JsonSubTypes.Type(AgentDataObject.class),
        @JsonSubTypes.Type(ResourceGroupWatcherDataObject.class)
})
public abstract class AbstractDataObject<E extends EntityConfiguration> implements Exportable<E> {
    @JsonIgnore
    private final Map<String, String> parameters;

    /**
     * Instantiates a new Abstract dto entity.
     */
    AbstractDataObject() {
        parameters = new HashMap<>();
    }

    /**
     * Instantiates a new Abstract dto entity.
     *
     * @param entity The configurable entity to wrap.
     */
    AbstractDataObject(final E entity) {
        this.parameters = new HashMap<>(entity);
    }

    @JsonProperty
    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public final Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Exports state of this object into entity configuration.
     * @param entity Entity to modify.
     */
    @Override
    public void exportTo(final E entity){
        entity.load(parameters);
    }
}