package com.bytex.snamp.instrumentation;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Represents measurement of {@link String} data type.
 * @since 1.0
 * @version 1.0
 * @author Roman Sakno
 */
public final class StringMeasurement extends ValueMeasurement {
    private static final long serialVersionUID = 3212183719121919189L;
    private String value = "";

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(value);
        super.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        value = in.readUTF();
        super.readExternal(in);
    }

    @JsonIgnore
    public String getValue(final String existingValue){
        switch (getChangeType()){
            case SUM:
                return existingValue.concat(value);
            case SUB:
                return existingValue.replace(value, "");
            case MAX:
                return existingValue.compareTo(value) > 0 ? existingValue : value;
            case MIN:
                return existingValue.compareTo(value) < 0 ? existingValue : value;
            default:
                return value;
        }
    }

    @JsonProperty(VALUE_JSON_PROPERTY)
    public String getValue(){
        return value;
    }

    public void setValue(final String value){
        if(value != null)
            this.value = value;
        else
            throw new IllegalArgumentException("Value cannot be null");
    }
}
