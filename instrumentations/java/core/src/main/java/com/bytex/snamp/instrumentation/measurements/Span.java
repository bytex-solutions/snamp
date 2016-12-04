package com.bytex.snamp.instrumentation.measurements;

import com.bytex.snamp.instrumentation.Identifier;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Represents span.
 * @since 1.0
 * @version 1.0
 * @author Roman Sakno
 */
public final class Span extends TimeMeasurement {
    public static final CorrelationPolicy DEFAULT_CORRELATION_POLICY = CorrelationPolicy.LOCAL;

    private static final long serialVersionUID = -1873210335013467017L;
    private Identifier correlationID = Identifier.EMPTY;
    private Identifier spanID = Identifier.EMPTY;
    private Identifier parentSpanID = Identifier.EMPTY;
    private CorrelationPolicy correlationPolicy = DEFAULT_CORRELATION_POLICY;

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        correlationID.serialize(out);
        spanID.serialize(out);
        parentSpanID.serialize(out);
        out.writeUTF(correlationPolicy.name());
        super.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        correlationID = Identifier.deserialize(in);
        spanID = Identifier.deserialize(in);
        parentSpanID = Identifier.deserialize(in);
        correlationPolicy = CorrelationPolicy.valueOf(in.readUTF());
        super.readExternal(in);
    }

    @JsonProperty("correlType")
    public CorrelationPolicy getCorrelationPolicy(){
        return correlationPolicy;
    }

    public void setCorrelationPolicy(final CorrelationPolicy value){
        if(value == null)
            throw new IllegalArgumentException("value cannot be null");
        correlationPolicy = value;
    }

    public Identifier getCorrelationID(){
        return correlationID;
    }

    @JsonProperty("correl")
    public void setCorrelationID(final Identifier value) {
        if (value == null)
            throw new IllegalArgumentException();
        else
            correlationID = value;
    }

    @JsonProperty("s")
    public Identifier getSpanID(){
        return spanID;
    }

    public void setSpanID(final Identifier value) {
        if (value == null)
            throw new IllegalArgumentException();
        else
            spanID = value;
    }

    @JsonProperty("ps")
    public Identifier getParentSpanID(){
        return parentSpanID;
    }

    public void setParentSpanID(final Identifier value){
        if (value == null)
            throw new IllegalArgumentException();
        else
            parentSpanID = value;
    }
}
