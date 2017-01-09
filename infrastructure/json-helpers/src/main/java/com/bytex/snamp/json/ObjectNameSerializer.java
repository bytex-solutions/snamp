package com.bytex.snamp.json;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;

import javax.management.ObjectName;
import java.io.IOException;

/**
 * Provides serialization of {@link ObjectName} into JSON.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ObjectNameSerializer extends JsonSerializer<ObjectName> {
    /**
     * Method that can be called to ask implementation to serialize
     * values of type this serializer handles.
     *
     * @param value    Value to serialize; can <b>not</b> be null.
     * @param jgen     Generator used to output resulting Json content
     * @param provider Provider that can be used to get serializers for
     */
    @Override
    public void serialize(final ObjectName value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        jgen.writeString(value.getCanonicalName());
        ObjectMapper mapper;
    }
}
