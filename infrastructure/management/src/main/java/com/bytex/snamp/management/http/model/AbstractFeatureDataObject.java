package com.bytex.snamp.management.http.model;

import com.bytex.snamp.configuration.FeatureConfiguration;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.MapUtils.putValue;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonSubTypes({
        @JsonSubTypes.Type(EventDataObject.class),
        @JsonSubTypes.Type(AttributeDataObject.class),
        @JsonSubTypes.Type(OperationDataObject.class)
})
public abstract class AbstractFeatureDataObject<F extends FeatureConfiguration> extends AbstractDataObject<F> implements FeatureConfiguration {
    AbstractFeatureDataObject(){

    }

    AbstractFeatureDataObject(final F feature){
        super(feature);
    }

    @Override
    @JsonIgnore
    public final void setAlternativeName(final String value) {
        parameters.put(NAME_KEY, value);
    }

    @Override
    @JsonIgnore
    public final String getAlternativeName() {
        return parameters.get(NAME_KEY);
    }

    @Override
    @JsonIgnore
    public final boolean isAutomaticallyAdded() {
        return getValue(parameters, AUTOMATICALLY_ADDED_KEY, Boolean::parseBoolean).orElse(false);
    }

    @Override
    @JsonIgnore
    public final void setAutomaticallyAdded(final boolean value) {
        if (value)
            putValue(parameters, AUTOMATICALLY_ADDED_KEY, Boolean.TRUE, Object::toString);
        else
            parameters.remove(AUTOMATICALLY_ADDED_KEY);
    }
}
