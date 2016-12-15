package com.bytex.snamp.instrumentation.measurements;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Represents JSON deserializer for {@link java.util.concurrent.TimeUnit}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class TimeUnitDeserializer extends JsonDeserializer<TimeUnit> {
    private final Map<String, TimeUnit> binding;

    public TimeUnitDeserializer(){
        binding = new HashMap<>();
        for(final TimeUnit unit: TimeUnit.values())
            binding.put(TimeUnitSerializer.toString(unit), unit);
    }

    @Override
    public TimeUnit deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        return binding.get(jp.getText());
    }
}
