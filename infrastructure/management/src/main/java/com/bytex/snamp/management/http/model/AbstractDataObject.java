package com.bytex.snamp.management.http.model;

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
        @JsonSubTypes.Type(ManagedResourceDataObject.class),
        @JsonSubTypes.Type(GatewayDataObject.class),
        @JsonSubTypes.Type(ResourceGroupDataObject.class)
})
public abstract class AbstractDataObject<E extends EntityConfiguration> implements EntityConfiguration {
    @JsonIgnore
    protected final Map<String, String> parameters;

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

    @Override
    @JsonProperty
    public final Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public final void load(final Map<String, String> value) {
        parameters.clear();
        parameters.putAll(value);
    }

    /**
     * Exports state of this object into entity configuration.
     * @param entity Entity to modify.
     */
    public void exportTo(final E entity){
        entity.load(parameters);
    }

    @Override
    @JsonIgnore
    public final void setDescription(final String value) {
        this.parameters.put(DESCRIPTION_KEY, value);
    }

    @Override
    @JsonIgnore
    public final String getDescription() {
        return this.parameters.get(DESCRIPTION_KEY);
    }
}