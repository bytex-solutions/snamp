package com.bytex.snamp.connector.composite.functions;

import javax.management.openmbean.SimpleType;
import java.util.Collection;
import java.util.Objects;

/**
 * Extracts field from the composite data.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ExtractAsStringFunction extends AbstractExtractFunction<String> {

    ExtractAsStringFunction(final Collection<String> path) {
        super(path, SimpleType.STRING);
    }

    @Override
    String convert(final Object value) {
        return Objects.toString(value);
    }

    @Override
    String getFallbackValue() {
        return "";
    }
}
