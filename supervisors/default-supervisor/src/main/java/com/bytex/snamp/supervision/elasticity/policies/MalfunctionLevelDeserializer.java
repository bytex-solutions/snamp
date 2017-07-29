package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.connector.health.MalfunctionStatus;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

/**
 * Provides deserialization of {@link MalfunctionStatus.Level} from JSON.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class MalfunctionLevelDeserializer extends JsonDeserializer<MalfunctionStatus.Level> {
    @Override
    public MalfunctionStatus.Level deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        return MalfunctionStatus.Level.valueOf(jp.getText());
    }
}
