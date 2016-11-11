package com.bytex.snamp.instrumentation;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

/**
 * Represents deserializer for {@link ChangeType}.
 */
final class ChangeTypeDeserializer extends JsonDeserializer<ChangeType> {
    @Override
    public ChangeType deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        final String jsonValue = jp.getText();
        for(final ChangeType type : ChangeType.values())
            if(type.getJsonValue().equals(jsonValue))
                return type;
        return ChangeType.NEW_VALUE;
    }
}
