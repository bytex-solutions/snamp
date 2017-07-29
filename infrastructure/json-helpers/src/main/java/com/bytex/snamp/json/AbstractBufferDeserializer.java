package com.bytex.snamp.json;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.node.ArrayNode;

import java.io.IOException;
import java.nio.Buffer;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class AbstractBufferDeserializer<B extends Buffer> extends JsonDeserializer<B> {

    protected abstract B deserialize(final ArrayNode input) throws JsonProcessingException;

    /**
     * Method that can be called to ask implementation to deserialize
     * JSON content into the value type this serializer handles.
     * Returned instance is to be constructed by method itself.
     * <p>
     * Pre-condition for this method is that the parser points to the
     * first event that is part of value to deserializer (and which
     * is never JSON 'null' literal, more on this below): for simple
     * types it may be the only value; and for structured types the
     * Object start marker.
     * Post-condition is that the parser will point to the last
     * event that is part of deserialized value (or in case deserialization
     * fails, event that was not recognized or usable, which may be
     * the same event as the one it pointed to upon call).
     * <p>
     * Note that this method is never called for JSON null literal,
     * and thus deserializers need (and should) not check for it.
     *
     * @param jp   Parsed used for reading JSON content
     * @param ctxt Context that can be used to access information about
     *             this deserialization activity.
     * @return Deserializer value
     */
    @Override
    public final B deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        final B result = deserialize(jp.readValueAs(ArrayNode.class));
        result.rewind();
        return result;
    }
}
