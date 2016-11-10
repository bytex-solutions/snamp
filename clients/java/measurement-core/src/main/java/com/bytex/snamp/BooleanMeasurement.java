package com.bytex.snamp;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Represents measurement of {@code boolean} data type.
 * @since 1.0
 * @version 1.0
 * @author Roman Sakno
 */
public final class BooleanMeasurement extends Measurement {
    private static final long serialVersionUID = -2769042034301266820L;
    private boolean value;

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeBoolean(value);
        super.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readBoolean();
        super.readExternal(in);
    }

    @JsonProperty(VALUE_JSON_PROPERTY)
    public boolean getValue(){
        return value;
    }

    public void setValue(final boolean value){
        this.value = value;
    }
}
