package com.bytex.snamp.connector.composite.functions;

import javax.management.openmbean.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class AbstractExtractFunction<T> extends AggregationFunction<T> {
    private final Collection<String> path;

    AbstractExtractFunction(final Collection<String> path, final SimpleType<T> returnType) {
        super(returnType);
        this.path = Objects.requireNonNull(path);
    }

    /**
     * Detects valid input type for this function.
     *
     * @param index     Parameter position.
     * @param inputType Input type to check.
     * @return {@literal true}, if this function can accept a value of the specified type; otherwise, {@literal false}.
     */
    @Override
    public final boolean canAccept(final int index, final OpenType<?> inputType) {
        return index == 0 && inputType instanceof CompositeType;
    }

    private static Object extract(CompositeData data, final Iterator<String> path) throws OpenDataException {
        Object field = data;
        do {
            data = (CompositeData)field;
            final String fieldName = path.next();
            if (data.containsKey(fieldName))
                field = data.get(fieldName);
            else
                throw new OpenDataException(String.format("Field %s doesn't exist in %s", fieldName, data));

        } while (path.hasNext() && field instanceof CompositeData);

        return field;
    }

    abstract T convert(final Object value);

    abstract T getFallbackValue();

    @Override
    public final T invoke(final NameResolver resolver, final Object... args) throws OpenDataException, NumberFormatException {
        if (args.length > 0 && !path.isEmpty()) {
            Object arg = args[0];
            arg = arg instanceof CompositeData ? extract((CompositeData) arg, path.iterator()) : arg;
            return convert(arg);
        }
        return getFallbackValue();
    }
}
