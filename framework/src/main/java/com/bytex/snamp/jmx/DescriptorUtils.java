package com.bytex.snamp.jmx;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.google.common.collect.Maps;

import javax.management.Descriptor;
import javax.management.DescriptorRead;
import javax.management.ImmutableDescriptor;
import javax.management.JMX;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents utility methods for working with {@link javax.management.Descriptor} instances.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class DescriptorUtils {
    public static final String DEFAULT_VALUE_FIELD  = JMX.DEFAULT_VALUE_FIELD;
    public static final String MIN_VALUE_FIELD = JMX.MIN_VALUE_FIELD;
    public static final String MAX_VALUE_FIELD = JMX.MAX_VALUE_FIELD;
    public static final String UNIT_OF_MEASUREMENT_FIELD = AttributeConfiguration.UNIT_OF_MEASUREMENT_KEY;

    public static final DescriptorRead EMPTY_DESCRIPTOR = () -> ImmutableDescriptor.EMPTY_DESCRIPTOR;

    private DescriptorUtils(){
        throw new InstantiationError();
    }

    private static <T> Optional<T> getField(final Descriptor descr,
                                 final String fieldName,
                                 final Predicate<Object> valueFilter,
                                 final Function<Object, ? extends T> transform) {
        if (descr == null)
            return Optional.empty();
        final Object fieldValue = descr.getFieldValue(fieldName);
        if (fieldValue == null)
            return Optional.empty();
        else if (valueFilter.test(fieldValue))
            return Optional.of(fieldValue).map(transform);
        else if (fieldValue.getClass().isArray())
            if (Array.getLength(fieldValue) > 0) {
                final Object item = Array.get(fieldValue, 0);
                if (valueFilter.test(item))
                    return Optional.of(item).map(transform);
            }
        return Optional.empty();
    }

    public static <T> Optional<T> getField(final Descriptor descr,
                                  final String fieldName,
                                  final Function<Object, ? extends T> transform) {
        return getField(descr, fieldName, value -> true, transform);
    }

    public static <T> Optional<T> parseStringField(final Descriptor descr,
                                         final String fieldName,
                                         final Function<String, ? extends T> transform) {
        return getField(descr, fieldName, value -> value instanceof String, value -> transform.apply((String) value));
    }

    public static <T, E extends Throwable> T getFieldIfPresent(final Descriptor descr,
                                 final String fieldName,
                                 final Function<Object, ? extends T> transform,
                                 final Function<String, ? extends E> exceptionFactory) throws E {
        if (descr == null)
            throw exceptionFactory.apply(fieldName);
        final Object fieldValue = descr.getFieldValue(fieldName);
        if (fieldValue == null)
            throw exceptionFactory.apply(fieldName);
        else if (fieldValue.getClass().isArray()) {
            if (Array.getLength(fieldValue) > 0)
                return transform.apply(Array.get(fieldValue, 0));
        } else
            return transform.apply(fieldValue);
        throw exceptionFactory.apply(fieldName);
    }

    private static Properties toProperties(final Descriptor descr) {
        final Properties result = new Properties();
        if (descr != null)
            for (final String fieldName : descr.getFieldNames()) {
                final Object fieldValue = descr.getFieldValue(fieldName);
                if (fieldValue != null)
                    result.setProperty(fieldName, fieldValue.toString());
            }
        return result;
    }

    public static String toString(final Descriptor descr, final String comments) throws IOException {
        final Properties props = toProperties(descr);
        try(final StringWriter writer = new StringWriter(1024)){
            props.store(writer, comments);
            return writer.toString();
        }
    }

    /**
     * Converts {@link Descriptor} to map of fields.
     * @param descr The descriptor to convert. May be {@literal null}.
     * @return A map of fields.
     */
    public static Map<String, ?> toMap(final Descriptor descr){
        return toMap(descr, false);
    }

    /**
     * Converts {@link Descriptor} to map of fields.
     * @param descr The descriptor to convert. May be {@literal null}.
     * @param ignoreNullValues {@literal true} if null values from descriptor should not be copied into output map.
     * @return A map of fields.
     */
    public static Map<String, Object> toMap(final Descriptor descr, final boolean ignoreNullValues){
        return toMap(descr, Function.identity(), ignoreNullValues);
    }

    /**
     * Converts {@link Descriptor} to map of fields.
     * @param descr The descriptor to convert. May be {@literal null}.
     * @param valueInterceptor Function used to intercept and modify values from descriptor.
     * @param ignoreNullValues {@literal true} if null values from descriptor should not be copied into output map.
     * @return A map of fields.
     */
    public static Map<String, Object> toMap(final Descriptor descr, final Function<Object, Object> valueInterceptor, final boolean ignoreNullValues) {
        if (descr == null) return Collections.emptyMap();
        final String[] fields = descr.getFieldNames();
        final Map<String, Object> result = Maps.newHashMapWithExpectedSize(fields.length);
        for (final String fieldName : fields) {
            Object fieldValue = descr.getFieldValue(fieldName);
            if (fieldValue == null) {
                if (ignoreNullValues)
                    continue;
            } else
                fieldValue = valueInterceptor.apply(fieldValue);
            result.put(fieldName, fieldValue);
        }
        return result;
    }

    /**
     * Creates {@link Dictionary} facade for the specified {@link Descriptor} instance.
     * @param descr Descriptor object to wrap.
     * @return A dictionary backed by {@link Descriptor} instance.
     */
    public static Dictionary<String, ?> asDictionary(final Descriptor descr){
        return new DescriptorDictionary(descr);
    }

    /**
     * Determines whether {@link Descriptor} instance has the specified field.
     * @param descr The descriptor. Cannot be {@literal null}.
     * @param fieldName The name of the field.
     * @return {@literal true}, if {@link Descriptor} instance has the field; otherwise, {@literal false}.
     */
    public static boolean hasField(final Descriptor descr, final String fieldName){
        return ArrayUtils.contains(descr.getFieldNames(), fieldName);
    }

    public static <T> T getDefaultValue(final Descriptor descr, final Class<T> type){
        final Object value = descr.getFieldValue(DEFAULT_VALUE_FIELD);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public static Object getRawMaxValue(final Descriptor descr){
        return descr.getFieldValue(MAX_VALUE_FIELD);
    }

    public static Object getRawMinValue(final Descriptor descr){
        return descr.getFieldValue(MIN_VALUE_FIELD);
    }

    public static String getUOM(final Descriptor descr){
        return getField(descr, UNIT_OF_MEASUREMENT_FIELD, Objects::toString).orElse("");
    }
}
