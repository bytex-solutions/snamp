package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.moa.ReduceOperation;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Serializes {@link ReduceOperation} into JSON.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ReduceOperationSerializer extends JsonSerializer<ReduceOperation> {
    @Override
    public void serialize(final ReduceOperation value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        jgen.writeString(value.name());
    }
}
