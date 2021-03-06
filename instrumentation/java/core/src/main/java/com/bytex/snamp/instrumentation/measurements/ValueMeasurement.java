package com.bytex.snamp.instrumentation.measurements;

import org.codehaus.jackson.annotate.JsonIgnore;
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

    /**
     * Represents simple type name of the value supplied by this measurement.
     * @return Type name.
     */
    @JsonIgnore
    public abstract Class<? extends Comparable<?>> getType();

    /**
     * Gets value provided by this measurement.
     * @return Measurement value.
     */
    @JsonIgnore
    public abstract Comparable<?> getRawValue();

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
