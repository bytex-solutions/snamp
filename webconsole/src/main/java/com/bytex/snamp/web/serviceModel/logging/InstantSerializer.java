package com.bytex.snamp.web.serviceModel.logging;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;
import java.time.Instant;

/**
 * Represents serializer for data type {@link Instant}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class InstantSerializer extends JsonSerializer<Instant> {
    @Override
    public void serialize(final Instant value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        jgen.writeString(value.toString());
    }
}
