package com.bytex.snamp.web.serviceModel.managedResources;

import com.bytex.snamp.jmx.WellKnownType;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Serializes {@link com.bytex.snamp.jmx.WellKnownType} into JSON.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class WellKnownTypeSerializer extends JsonSerializer<WellKnownType> {
    @Override
    public void serialize(final WellKnownType value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        jgen.writeString(value.getDisplayName());
    }
}
