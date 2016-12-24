package com.bytex.snamp.web.serviceModel.logging;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Represents JSON serializer for {@link LogLevel}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class LogLevelSerializer extends JsonSerializer<LogLevel> {
    @Override
    public void serialize(final LogLevel value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        jgen.writeString(value.toString());
    }
}
