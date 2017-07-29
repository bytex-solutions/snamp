package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.moa.ReduceOperation;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

/**
 * Deserializes {@link ReduceOperation} from JSON.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class ReduceOperationDeserializer extends JsonDeserializer<ReduceOperation> {
    @Override
    public ReduceOperation deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        return ReduceOperation.valueOf(jp.getText());
    }
}
