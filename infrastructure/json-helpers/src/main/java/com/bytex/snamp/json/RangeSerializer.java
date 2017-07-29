package com.bytex.snamp.json;

import com.google.common.collect.Range;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * Provides serialization of {@link Range} into JSON.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class RangeSerializer extends JsonSerializer<Range> {
    @Override
    public void serialize(final Range value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        jgen.writeString(value.toString());
    }
}
