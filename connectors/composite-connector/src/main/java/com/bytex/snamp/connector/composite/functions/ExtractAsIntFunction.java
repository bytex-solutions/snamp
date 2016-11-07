package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.Convert;

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
        return Convert.toLong(value);
    }

    @Override
    Long getFallbackValue() {
        return 0L;
    }
}
