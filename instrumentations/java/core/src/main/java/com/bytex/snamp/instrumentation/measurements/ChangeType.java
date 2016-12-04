package com.bytex.snamp.instrumentation.measurements;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Represents type of modification that should be applied to the resulting measurement.
 */
@JsonSerialize(using = ChangeTypeSerializer.class)
@JsonDeserialize(using = ChangeTypeDeserializer.class)
public enum ChangeType {
    /**
     * A new value must be computed as addition of newly supplied and existing value.
     */
    SUM("sum"),

    /**
     * A new value must be computed as subtraction of newly supplied and existing value.
     */
    SUB("sub"),

    /**
     * A new value must be computed as max value between newly supplied and existing value.
     */
    MAX("max"),

    /**
     * A new value must be computed as min value between newly supplied and existing value.
     */
    MIN("min"),

    /**
     * Existing value will be replaced with newly supplied value.
     */
    NEW_VALUE("value");

    private final String jsonValue;

    ChangeType(final String jsonValue){
        this.jsonValue = jsonValue;
    }

    String getJsonValue(){
        return jsonValue;
    }
}
