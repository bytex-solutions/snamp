package com.bytex.snamp.web.serviceModel.logging;

import com.bytex.snamp.connector.notifications.Severity;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Represents JSON serializer for {@link Severity}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class SeveritySerializer extends JsonSerializer<Severity> {
    @Override
    public void serialize(final Severity value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        jgen.writeString(value.toString());
    }
}
