package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.connector.metrics.MetricsInterval;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Provides serialization of {@link MetricsInterval} into JSON.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class MetricIntervalSerializer extends JsonSerializer<MetricsInterval> {
    @Override
    public void serialize(final MetricsInterval value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        jgen.writeString(value.name().toLowerCase());
    }
}
