package com.bytex.snamp.json;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import javax.management.openmbean.CompositeData;
import java.io.IOException;

/**
 * Provides serialization of {@link CompositeData} into JSON.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class CompositeDataSerializer extends JsonSerializer<CompositeData> {
    /**
     * Method that can be called to ask implementation to serialize
     * values of type this serializer handles.
     *
     * @param value    Value to serialize; can <b>not</b> be null.
     * @param jgen     Generator used to output resulting Json content
     * @param provider Provider that can be used to get serializers for
     */
    @Override
    public void serialize(final CompositeData value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        ComplexTypeFormatter.serialize(value).serialize(jgen, provider);
    }
}
