package com.bytex.snamp.instrumentation.measurements;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Represents JSON serializer for {@link java.util.concurrent.TimeUnit}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class TimeUnitSerializer extends JsonSerializer<TimeUnit> {
    @Override
    public void serialize(final TimeUnit value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        jgen.writeString(toString(value));
    }

    static String toString(final TimeUnit unit){
        return unit.name().toLowerCase();
    }
}
