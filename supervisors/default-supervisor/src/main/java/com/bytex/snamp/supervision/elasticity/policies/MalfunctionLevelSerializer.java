package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.connector.health.MalfunctionStatus;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Provides serialization of {@link MalfunctionStatus.Level} into JSON.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class MalfunctionLevelSerializer extends JsonSerializer<MalfunctionStatus.Level> {
    @Override
    public void serialize(final MalfunctionStatus.Level value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        jgen.writeString(value.name());
    }
}
