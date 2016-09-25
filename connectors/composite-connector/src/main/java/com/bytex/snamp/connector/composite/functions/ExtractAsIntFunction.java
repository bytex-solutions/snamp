package com.bytex.snamp.connector.composite.functions;

import javax.management.openmbean.SimpleType;
import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ExtractAsIntFunction extends AbstractExtractFunction<Long> {
    ExtractAsIntFunction(final Collection<String> path) {
        super(path, SimpleType.LONG);
    }

    @Override
    Long convert(final Object value) {
        if (value instanceof Number)
            return ((Number) value).longValue();
        else if (value != null)
            return Long.parseLong(value.toString());
        else
            return getFallbackValue();
    }

    @Override
    Long getFallbackValue() {
        return 0L;
    }
}
