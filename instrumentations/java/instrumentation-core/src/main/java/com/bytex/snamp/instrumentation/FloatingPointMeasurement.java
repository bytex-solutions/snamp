package com.bytex.snamp.instrumentation;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Represents measurement of {@code double} data type.
 * @since 1.0
 * @version 1.0
 * @author Roman Sakno
 */
public class FloatingPointMeasurement extends ValueMeasurement {
    private static final long serialVersionUID = -5453349320908165683L;
    private double value;

    /**
     * Gets value provided by this measurement.
     *
     * @return Measurement value.
     */
    @Override
    @JsonIgnore
    public Double getRawValue() {
        return value;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeDouble(value);
        super.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readDouble();
        super.readExternal(in);
    }

    @JsonIgnore
    public double getValue(final double existingValue){
        switch (getChangeType()){
            case SUB:
                return existingValue - value;
            case SUM:
                return existingValue + value;
            case MAX:
                return Math.max(value, existingValue);
            case MIN:
                return Math.min(value, existingValue);
            default:
                return value;
        }
    }

    @JsonProperty(VALUE_JSON_PROPERTY)
    public double getValue(){
        return value;
    }

    public void setValue(final double value){
        this.value = value;
    }
}
