package com.bytex.snamp.instrumentation.measurements;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents JSON deserializer for {@link CorrelationPolicy}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CorrelationDeserializer extends JsonDeserializer<CorrelationPolicy> {
    private final Map<String, CorrelationPolicy> binding;

    public CorrelationDeserializer(){
        binding = new HashMap<>();
        for(final CorrelationPolicy c: CorrelationPolicy.values())
            binding.put(c.toString(), c);
    }

    @Override
    public CorrelationPolicy deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        return binding.get(jp.getText());
    }
}
