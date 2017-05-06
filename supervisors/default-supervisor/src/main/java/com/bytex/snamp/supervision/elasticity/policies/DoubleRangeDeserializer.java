package com.bytex.snamp.supervision.elasticity.policies;

import com.bytex.snamp.moa.RangeUtils;
import com.google.common.collect.Range;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

/**
 * Deserializes {@link Range} from JSON.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class DoubleRangeDeserializer extends JsonDeserializer<Range<Double>> {
    @Override
    public Range<Double> deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
        return RangeUtils.parseDoubleRange(jp.getText());
    }
}
