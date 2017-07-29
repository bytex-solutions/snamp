package com.bytex.snamp.web.serviceModel.logging;

import com.bytex.snamp.connector.notifications.Severity;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

/**
 * Represents deserializer for {@link Severity}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class SeverityDeserializer extends JsonDeserializer<Severity> {

    @Override
    public Severity deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        return Severity.resolve(jp.getText());
    }
}
