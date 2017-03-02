package com.bytex.snamp.web.serviceModel.watcher;

import com.bytex.snamp.connector.supervision.HealthStatus;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Provides serialization of {@link HealthStatus} into JSON.
 */
final class HealthStatusSerializer extends JsonSerializer<HealthStatus> {
    @Override
    public void serialize(final HealthStatus value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {

    }
}
