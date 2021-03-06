package com.bytex.snamp.instrumentation.measurements;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Provides health status.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@JsonTypeName("healthCheck")
public final class Health extends Measurement {
    private static final long serialVersionUID = -5234694817709939668L;

    /**
     * Represents health status.
     */
    public enum Status{
        UP{
            @Override
            public String toString() {
                return "Component or subsystem is functioning as expected";
            }
        },
        DOWN{
            @Override
            public String toString() {
                return "Component or subsystem has suffered an unexpected failure";
            }
        },
        OUT_OF_SERVICE{
            @Override
            public String toString() {
                return "Component or subsystem has been taken out of service and should not be used";
            }
        }
    }

    private Status status;

    public Health() {
        status = Status.UP;
    }

    @JsonProperty("status")
    public Status getStatus(){
        return status;
    }

    public void setStatus(final Status value) {
        if (status == null)
            throw new IllegalArgumentException();
        else
            status = value;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeObject(status);
        super.writeExternal(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        status = (Status) in.readObject();
        super.readExternal(in);
    }
}
