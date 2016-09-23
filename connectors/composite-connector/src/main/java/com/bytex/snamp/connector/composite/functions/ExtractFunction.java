package com.bytex.snamp.connector.composite.functions;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * Extracts field from the composite data.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ExtractFunction extends AggregationFunction<String> {
    private final Collection<String> path;

    ExtractFunction(final Collection<String> path) {
        super(SimpleType.STRING);
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
    public boolean canAccept(final int index, final OpenType<?> inputType) {
        return index == 0 && inputType instanceof CompositeType;
    }

    private static String extract(final CompositeData data, final Iterator<String> path) {
        Object field;
        do {
            field = data.get(path.next());
        } while (path.hasNext() && field instanceof CompositeData);

        return Objects.toString(field);
    }

    @Override
    public String invoke(final NameResolver resolver, final Object... args) {
        if(args.length > 0 && !path.isEmpty()){
            final Object arg = args[0];
            return arg instanceof CompositeData ? extract((CompositeData)arg, path.iterator()) : Objects.toString(arg);
        } else
            return "";
    }
}
