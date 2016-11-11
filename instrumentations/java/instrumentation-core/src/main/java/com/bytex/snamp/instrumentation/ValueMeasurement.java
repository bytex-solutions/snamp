package com.bytex.snamp.instrumentation;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Represents abstract measurement which holds the value.
 */
public abstract class ValueMeasurement extends Measurement {
    private static final long serialVersionUID = -5839389918277380257L;
    private ChangeType changeType;

    ValueMeasurement(){
        changeType = ChangeType.NEW_VALUE;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(changeType.name());
        super.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        changeType = ChangeType.valueOf(in.readUTF());
        super.readExternal(in);
    }

    @JsonProperty("ct")
    public final ChangeType getChangeType(){
        return changeType;
    }

    public void setChangeType(final ChangeType value) {
        if (value != null)
            changeType = value;
        else
            throw new IllegalArgumentException();
    }
}
