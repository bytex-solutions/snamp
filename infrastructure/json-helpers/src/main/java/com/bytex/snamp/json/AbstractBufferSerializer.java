package com.bytex.snamp.json;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.node.ArrayNode;

import java.io.IOException;
import java.nio.Buffer;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractBufferSerializer<B extends Buffer> extends JsonSerializer<B> {

    protected abstract void serialize(final B input, final ArrayNode output);

    /**
     * Method that can be called to ask implementation to serialize
     * values of type this serializer handles.
     *
     * @param value    Value to serialize; can <b>not</b> be null.
     * @param jgen     Generator used to output resulting Json content
     * @param provider Provider that can be used to get serializers for
     */
    @Override
    public final void serialize(final B value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        final ArrayNode node = ThreadLocalJsonFactory.getFactory().arrayNode();
        serialize(value, node);
        node.serialize(jgen, provider);
    }
}
