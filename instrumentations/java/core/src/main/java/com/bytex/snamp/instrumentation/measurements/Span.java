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
    private static final long serialVersionUID = -1873210335013467017L;
    private Identifier correlationID = Identifier.EMPTY;
    private Identifier spanID = Identifier.EMPTY;
    private Identifier parentSpanID = Identifier.EMPTY;
    private String subsystemName = "";

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(subsystemName);
        correlationID.serialize(out);
        spanID.serialize(out);
        parentSpanID.serialize(out);
        super.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        subsystemName = in.readUTF();
        correlationID = Identifier.deserialize(in);
        spanID = Identifier.deserialize(in);
        parentSpanID = Identifier.deserialize(in);
        super.readExternal(in);
    }

    public void generateIDs(){
        correlationID = Identifier.randomID();
        spanID = Identifier.randomID();
    }

    /**
     * Gets module inside of the reporting application which reports this span.
     * <p>
     *     Subsystem name allows to represent interoperability between different subsystems/modules
     *     inside
     * @return Reporting module inside of this application.
     */
    @JsonProperty("mn")
    public String getModuleName(){
        return subsystemName;
    }

    /**
     * Sets module inside of the reporting application which reports this span.
     * @param name Name of the module. Cannot be {@literal null}.
     */
    public void setModuleName(final String name) {
        if (name == null)
            throw new IllegalArgumentException();
        else
            subsystemName = name;
    }

    public Identifier getCorrelationID(){
        return correlationID;
    }

    @JsonProperty("cid")
    public void setCorrelationID(final Identifier value) {
        if (value == null)
            throw new IllegalArgumentException();
        else
            correlationID = value;
    }

    @JsonProperty("id")
    public Identifier getSpanID(){
        return spanID;
    }

    public void setSpanID(final Identifier value) {
        if (value == null)
            throw new IllegalArgumentException();
        else
            spanID = value;
    }

    @JsonProperty("pid")
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
