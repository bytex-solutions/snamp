package com.bytex.snamp.instrumentation;

import com.bytex.snamp.Identifier;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

/**
 * Provides deserialization of {@link com.bytex.snamp.Identifier}.
 */
final class IdentifierDeserializer extends JsonDeserializer<Identifier> {
    @Override
    public Identifier deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        return Identifier.ofString(jp.getText());
    }
}
