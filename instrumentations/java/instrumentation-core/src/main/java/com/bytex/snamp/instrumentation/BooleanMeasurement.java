package com.bytex.snamp.instrumentation;

import org.codehaus.jackson.annotate.JsonIgnore;
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
public final class BooleanMeasurement extends ValueMeasurement {
    private static final long serialVersionUID = -2769042034301266820L;
    private boolean value;

    public BooleanMeasurement(final boolean value){
        this.value = value;
    }

    public BooleanMeasurement(){
        this(false);
    }

    /**
     * Represents simple type name of the value supplied by this measurement.
     *
     * @return Type name.
     */
    @Override
    @JsonIgnore
    public Class<Boolean> getType() {
        return boolean.class;
    }

    /**
     * Gets value provided by this measurement.
     *
     * @return Measurement value.
     */
    @Override
    @JsonIgnore
    public Boolean getRawValue() {
        return value;
    }

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

    @JsonIgnore
    public boolean getValue(final boolean existingValue){
        switch (getChangeType()){
            case SUM:
            case MAX:
                return value | existingValue;
            case SUB:
            case MIN:
                return value & existingValue;
            default:
                return value;
        }
    }

    @JsonProperty(VALUE_JSON_PROPERTY)
    public boolean getValue(){
        return value;
    }

    public void setValue(final boolean value){
        this.value = value;
    }
}
