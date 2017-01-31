package com.bytex.snamp.instrumentation.measurements;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Represents JSON serializer for {@link ChangeType}.
 */
final class ChangeTypeSerializer extends JsonSerializer<ChangeType> {
    @Override
    public void serialize(final ChangeType value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        jgen.writeString(value.getJsonValue());
    }
}
