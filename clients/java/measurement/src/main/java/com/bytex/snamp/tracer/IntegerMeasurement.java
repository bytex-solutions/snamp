package com.bytex.snamp.tracer;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Represents measurement of {@code long} data type.
 * @since 1.0
 * @version 1.0
 * @author Roman Sakno
 */
@JsonTypeName("gauge64")
public final class IntegerMeasurement extends Measurement {
    private long value;

    /**
     * Gets measured value.
     * @return Measured value.
     */
    @JsonProperty("v")
    public long getValue(){
        return value;
    }

    /**
     * Sets measured value.
     * @param value Measured value.
     */
    public void setValue(final long value){
        this.value = value;
    }
}
