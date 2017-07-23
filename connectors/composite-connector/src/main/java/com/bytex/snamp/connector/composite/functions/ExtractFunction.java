package com.bytex.snamp.connector.composite.functions;

import com.bytex.snamp.jmx.OpenTypes;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.collect.ImmutableList;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * Extracts field from the composite attribute.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ExtractFunction extends AggregationFunction {
    private final WellKnownType targetType;
    private final ImmutableList<String> path;

    @SuppressWarnings("unchecked")
    ExtractFunction(final WellKnownType type, final Collection<String> path) {
        super(type.getOpenType());
        this.path = ImmutableList.copyOf(path);
        targetType = Objects.requireNonNull(type);
    }

    /**
     * Detects valid input type for this function.
     *
     * @param index     Parameter position.
     * @param inputType Input type to check.
     * @return {@literal true}, if this function can accept a value of the specified type; otherwise, {@literal false}.
     */
    @Override
    public boolean canAccept(final int index, final OpenType inputType) {
        return targetType.getOpenType().equals(inputType);
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

    /**
     * Invokes aggregation function.
     *
     * @param context A function used to resolve operands.
     * @param args     Arguments of the function.
     * @return Function result.
     * @throws IllegalArgumentException Unsupported input value.
     * @throws IllegalStateException    Unresolved operand.
     */
    @Override
    public Object eval(final EvaluationContext context, final Object... args) throws Exception {
        if (args.length > 0 && !path.isEmpty()) {
            Object arg = args[0];
            arg = arg instanceof CompositeData ? extract((CompositeData) arg, path.iterator()) : arg;
            return targetType.convert(arg);
        }
        return OpenTypes.defaultValue(targetType.getOpenType());
    }
}
