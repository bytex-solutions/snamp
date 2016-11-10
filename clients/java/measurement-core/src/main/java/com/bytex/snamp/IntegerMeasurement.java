package com.bytex.snamp;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Represents measurement of {@code long} data type.
 * @since 1.0
 * @version 1.0
 * @author Roman Sakno
 */
@JsonTypeName("gauge64")
public final class IntegerMeasurement extends Measurement {
    private static final long serialVersionUID = 352280955315548002L;
    private long value;

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeLong(value);
        super.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readLong();
        super.readExternal(in);
    }

    /**
     * Gets measured value.
     * @return Measured value.
     */
    @JsonProperty(VALUE_JSON_PROPERTY)
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
