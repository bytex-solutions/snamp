package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.Convert;

import javax.management.openmbean.SimpleType;
import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ExtractAsDoubleFunction extends AbstractExtractFunction<Double> {

    ExtractAsDoubleFunction(final Collection<String> path) {
        super(path, SimpleType.DOUBLE);
    }

    @Override
    Double convert(final Object value) throws NumberFormatException {
        return Convert.toDouble(value);
    }

    @Override
    Double getFallbackValue() {
        return Double.NaN;
    }
}
