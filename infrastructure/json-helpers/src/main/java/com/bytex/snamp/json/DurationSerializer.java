package com.bytex.snamp.json;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.time.Duration;

/**
 * Serializes {@link java.time.Duration} into JSON.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class DurationSerializer extends JsonSerializer<Duration> {
    @Override
    public void serialize(final Duration value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        jgen.writeString(value.toString());
    }
}
