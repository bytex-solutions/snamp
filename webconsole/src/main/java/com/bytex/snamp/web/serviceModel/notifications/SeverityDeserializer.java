package com.bytex.snamp.web.serviceModel.notifications;

import com.bytex.snamp.connector.notifications.Severity;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

/**
 * Provides deserialization of {@link com.bytex.snamp.connector.notifications.Severity} from JSON.
 */
final class SeverityDeserializer extends JsonDeserializer<Severity> {
    @Override
    public Severity deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        return Severity.resolve(jp.getText());
    }
}
