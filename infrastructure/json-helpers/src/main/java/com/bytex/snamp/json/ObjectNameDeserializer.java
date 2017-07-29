package com.bytex.snamp.json;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;

/**
 * Provides deserialization of {@link ObjectName} from JSON.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class ObjectNameDeserializer extends JsonDeserializer<ObjectName> {
    private static final class ObjectNameProcessingException extends JsonProcessingException{
        private static final long serialVersionUID = -8059352898904962577L;

        private ObjectNameProcessingException(final MalformedObjectNameException e){
            super(e);
        }
    }

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
    public ObjectName deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        try {
            return new ObjectName(jp.getText());
        } catch (final MalformedObjectNameException e) {
            throw new ObjectNameProcessingException(e);
        }
    }
}
