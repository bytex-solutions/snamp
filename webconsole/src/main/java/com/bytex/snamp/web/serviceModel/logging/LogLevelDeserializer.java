package com.bytex.snamp.web.serviceModel.logging;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents deserializer for {@link LogLevel}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class LogLevelDeserializer extends JsonDeserializer<LogLevel> {
    private final Map<String, LogLevel> levels;

    public LogLevelDeserializer(){
        levels = new HashMap<>();
        for(final LogLevel level: LogLevel.values())
            levels.put(level.toString(), level);
    }

    @Override
    public LogLevel deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        return levels.get(jp.getText());
    }
}
