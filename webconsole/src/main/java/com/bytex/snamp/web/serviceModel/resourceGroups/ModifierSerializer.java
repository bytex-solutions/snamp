package com.bytex.snamp.web.serviceModel.resourceGroups;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

import static com.bytex.snamp.supervision.GroupCompositionChanged.Modifier;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ModifierSerializer extends JsonSerializer<Modifier> {
    @Override
    public void serialize(final Modifier value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        jgen.writeString(value.name().toLowerCase());
    }
}
