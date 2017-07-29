package com.bytex.snamp.json;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.node.TextNode;

import java.io.IOException;
import java.time.Instant;

/**
 * Represents serializer for data type {@link Instant}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class InstantSerializer extends JsonSerializer<Instant> {
    public static TextNode serialize(final Instant value) {
        return ThreadLocalJsonFactory.getFactory().textNode(value.toString());
    }

    @Override
    public void serialize(final Instant value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        serialize(value).serialize(jgen, provider);
    }
}
