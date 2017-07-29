package com.bytex.snamp.web.serviceModel.commons;

import com.bytex.snamp.jmx.WellKnownType;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class WellKnownTypeDeserializer extends JsonDeserializer<WellKnownType> {
    @Override
    public WellKnownType deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        return WellKnownType.parse(jp.getText());
    }
}
