package com.bytex.snamp.tracer;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Represents measurement of {@code long} data type.
 */
@JsonTypeName("gauge64")
public final class IntegerMeasurement extends Measurement {
    private long value;

    @JsonProperty("v")
    public long getValue(){
        return value;
    }

    public void setValue(final long value){
        this.value = value;
    }
}
