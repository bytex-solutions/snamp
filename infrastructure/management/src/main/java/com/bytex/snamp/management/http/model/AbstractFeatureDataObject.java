package com.bytex.snamp.management.http.model;

import com.bytex.snamp.configuration.FeatureConfiguration;
import org.codehaus.jackson.annotate.JsonSubTypes;

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
    AbstractFeatureDataObject(){

    }

    AbstractFeatureDataObject(final F feature){
        super(feature);
    }
}
