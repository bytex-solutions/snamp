package com.bytex.snamp.json;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import javax.management.openmbean.OpenType;
import java.io.IOException;

/**
 * Provides deserialization of {@link OpenType} from JSON.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class OpenTypeDeserializer extends JsonDeserializer<OpenType> {
    @Override
    public OpenType deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        return OpenTypeFormatter.deserialize(jp.readValueAsTree());
    }
}
