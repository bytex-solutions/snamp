package com.bytex.snamp.web.serviceModel.notifications;

import com.bytex.snamp.connector.notifications.Severity;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Provides serialization of {@link Severity} into JSON.
 */
final class SeveritySerializer extends JsonSerializer<Severity> {
    @Override
    public void serialize(final Severity value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        jgen.writeString(value.toString());
    }
}
