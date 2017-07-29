package com.bytex.snamp.json;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;
import java.time.Duration;

/**
 * Deserializes {@link Duration} from JSON.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class DurationDeserializer extends JsonDeserializer<Duration> {
    @Override
    public Duration deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        return Duration.parse(jp.getText());
    }
}
