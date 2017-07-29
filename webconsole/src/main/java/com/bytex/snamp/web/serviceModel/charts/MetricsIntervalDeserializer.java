package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.connector.metrics.MetricsInterval;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

/**
 * Provides deserialization of {@link MetricsInterval} from JSON.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class MetricsIntervalDeserializer extends JsonDeserializer<MetricsInterval> {
    @Override
    public MetricsInterval deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        return MetricsInterval.valueOf(jp.getText().toUpperCase());
    }
}
