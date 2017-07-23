package com.bytex.snamp.json;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import javax.management.openmbean.OpenType;
import java.io.IOException;

/**
 * Provides serialization of {@link OpenType} into JSON.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class OpenTypeSerializer extends JsonSerializer<OpenType> {
    /**
     * Method that can be called to ask implementation to serialize
     * values of type this serializer handles.
     *
     * @param value    Value to serialize; can <b>not</b> be null.
     * @param jgen     Generator used to output resulting Json content
     * @param provider Provider that can be used to get serializers for
     */
    @Override
    public void serialize(final OpenType value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        OpenTypeFormatter.serialize(value).serialize(jgen, provider);
    }
}
