package com.bytex.snamp.instrumentation;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.TimeUnit;

/**
 * Represents measurement of time.
 * @since 1.0
 * @version 1.0
 * @author Roman Sakno
 */
public class TimeMeasurement extends Measurement {
    private static final long serialVersionUID = -6706383213005311530L;
    @JsonProperty("d")
    private long durationNanos;

    @JsonIgnore
    public final void setDuration(final long duration, final TimeUnit unit){
        durationNanos = unit.toNanos(duration);
    }

    public final long getDuration(final TimeUnit unit) {
        return durationNanos / unit.toNanos(1L);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeLong(durationNanos);
        super.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        durationNanos = in.readLong();
        super.readExternal(in);
    }
}
