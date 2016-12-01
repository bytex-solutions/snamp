package com.bytex.snamp.instrumentation;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Provides JSON serialization of
 */
final class IdentifierSerializer extends JsonSerializer<Identifier> {
    @Override
    public void serialize(final Identifier value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        value.serialize(jgen);
    }
}
