package com.bytex.snamp.instrumentation.measurements;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Represents JSON deserializer for {@link java.util.concurrent.TimeUnit}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class TimeUnitDeserializer extends JsonDeserializer<TimeUnit> {
    @Override
    public TimeUnit deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return TimeUnit.valueOf(jp.getText().toUpperCase());
    }
}
