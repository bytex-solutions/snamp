package com.bytex.snamp.connector.composite.functions;

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
        if (value instanceof Number)
            return ((Number) value).doubleValue();
        else if (value != null)
            return Double.parseDouble(value.toString());
        else
            return getFallbackValue();
    }

    @Override
    Double getFallbackValue() {
        return Double.NaN;
    }
}
