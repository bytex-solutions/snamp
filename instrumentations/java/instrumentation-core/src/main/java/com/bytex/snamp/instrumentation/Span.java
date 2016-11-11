package com.bytex.snamp.instrumentation;

import com.bytex.snamp.Identifier;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

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
    private static final long serialVersionUID = -1873210335013467017L;
    private Identifier correlationID = Identifier.EMPTY;
    private Identifier spanID = Identifier.EMPTY;
    private Identifier parentSpanID = Identifier.EMPTY;

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(correlationID.toBase64());
        out.writeUTF(spanID.toBase64());
        out.writeUTF(parentSpanID.toBase64());
        super.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        correlationID = Identifier.ofBase64(in.readUTF());
        spanID = Identifier.ofBase64(in.readUTF());
        parentSpanID = Identifier.ofBase64(in.readUTF());
        super.readExternal(in);
    }

    public Identifier getCorrelationID(){
        return correlationID;
    }

    @JsonProperty("correl")
    @JsonSerialize(using = IdentifierSerializer.class)
    @JsonDeserialize(using = IdentifierDeserializer.class)
    public void setCorrelationID(final Identifier value) {
        if (value == null)
            throw new IllegalArgumentException();
        else
            correlationID = value;
    }

    @JsonProperty("s")
    @JsonSerialize(using = IdentifierSerializer.class)
    @JsonDeserialize(using = IdentifierDeserializer.class)
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
    @JsonSerialize(using = IdentifierSerializer.class)
    @JsonDeserialize(using = IdentifierDeserializer.class)
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
