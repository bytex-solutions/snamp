package com.bytex.snamp.management.http.model;

import com.bytex.snamp.configuration.FeatureConfiguration;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

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
public abstract class AbstractFeatureDataObject<F extends FeatureConfiguration> extends AbstractDataObject<F> {
    private boolean overridden;

    AbstractFeatureDataObject(){

    }

    AbstractFeatureDataObject(final F feature){
        super(feature);
        overridden = feature.isOverridden();
    }

    @JsonProperty("override")
    public final boolean isOverridden(){
        return overridden;
    }

    public final void setOverridden(final boolean value){
        overridden = value;
    }

    /**
     * Exports state of this object into entity configuration.
     *
     * @param entity Entity to modify.
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void exportTo(@Nonnull final F entity) {
        super.exportTo(entity);
        entity.setOverridden(overridden);
    }
}
