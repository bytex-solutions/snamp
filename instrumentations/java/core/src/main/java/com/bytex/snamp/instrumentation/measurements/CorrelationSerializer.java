package com.bytex.snamp.instrumentation.measurements;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Represents JSON serializer for {@link CorrelationPolicy}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CorrelationSerializer extends JsonSerializer<CorrelationPolicy> {
    @Override
    public void serialize(final CorrelationPolicy value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        jgen.writeString(value.toString());
    }
}
