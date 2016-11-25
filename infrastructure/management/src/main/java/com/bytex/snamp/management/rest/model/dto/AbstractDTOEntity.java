package com.bytex.snamp.management.rest.model.dto;

import com.bytex.snamp.configuration.FeatureConfiguration;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashMap;
import java.util.Map;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.MapUtils.putValue;

/**
 * AbstractDTOEntity
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractDTOEntity implements FeatureConfiguration {
    private final Map<String, String> parameters;

    /**
     * Instantiates a new Abstract dto entity.
     */
    AbstractDTOEntity() {
        parameters = new HashMap<>();
    }

    /**
     * Instantiates a new Abstract dto entity.
     *
     * @param map the map
     */
    AbstractDTOEntity(final Map<String, String> map) {
        this.parameters = new HashMap<>(map);
    }

    @Override
    @JsonProperty
    public final Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public final void setParameters(final Map<String, String> value) {
        parameters.clear();
        parameters.putAll(value);
    }

    @Override
    @JsonIgnore
    public final void setDescription(final String value) {
        this.parameters.put(DESCRIPTION_KEY, value);
    }

    @Override
    @JsonIgnore
    public final void setAlternativeName(final String value) {
        this.parameters.put(NAME_KEY, value);
    }

    @Override
    @JsonIgnore
    public final String getDescription() {
        return this.parameters.get(DESCRIPTION_KEY);
    }

    @Override
    @JsonIgnore
    public final String getAlternativeName() {
        return this.parameters.get(NAME_KEY);
    }

    @Override
    @JsonIgnore
    public final boolean isAutomaticallyAdded() {
        return getValue(this.parameters, AUTOMATICALLY_ADDED_KEY, Boolean::parseBoolean, () -> false);
    }

    @Override
    @JsonIgnore
    public final void setAutomaticallyAdded(final boolean value) {
        if(value)
            putValue(this.parameters, AUTOMATICALLY_ADDED_KEY, Boolean.TRUE, Object::toString);
        else
            this.parameters.remove(AUTOMATICALLY_ADDED_KEY);
    }
}