package com.bytex.snamp.jmx;

import com.bytex.snamp.ArrayUtils;
import com.google.common.collect.Maps;

import javax.management.Descriptor;
import javax.management.DescriptorRead;
import javax.management.ImmutableDescriptor;
import javax.management.JMX;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Supplier;

/**
 * Represents utility methods for working with {@link javax.management.Descriptor} instances.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class DescriptorUtils {
    public static final String DEFAULT_VALUE_FIELD  = JMX.DEFAULT_VALUE_FIELD;
    public static final String LEGAL_VALUES_FIELD = JMX.LEGAL_VALUES_FIELD;
    public static final String MIN_VALUE_FIELD = JMX.MIN_VALUE_FIELD;
    public static final String MAX_VALUE_FIELD = JMX.MAX_VALUE_FIELD;
    public static final String UNIT_OF_MEASUREMENT_FIELD = "units";

    public static final DescriptorRead EMPTY_DESCRIPTOR = () -> ImmutableDescriptor.EMPTY_DESCRIPTOR;

    private DescriptorUtils(){
    }

    public static <T> T getField(final Descriptor descr,
                                 final String fieldName,
                                 final Class<T> fieldType,
                                 final Supplier<T> defval) {
        if(descr == null) return defval.get();
        final Object fieldValue = descr.getFieldValue(fieldName);
        if (fieldValue == null) return defval.get();
        else if (fieldType.isInstance(fieldValue))
            return fieldType.cast(fieldValue);
        else if (fieldValue.getClass().isArray())
            if (Array.getLength(fieldValue) > 0) {
                final Object item = Array.get(fieldValue, 0);
                if (fieldType.isInstance(item)) return fieldType.cast(item);
                else return defval.get();
            } else return defval.get();
        else return defval.get();
    }

    public static <T> T getField(final Descriptor descr,
                                 final String fieldName,
                                 final Class<T> fieldType,
                                 final T defval){
        return getField(descr, fieldName, fieldType, (Supplier<T>) () -> defval);
    }

    public static <T> T getField(final Descriptor descr,
                                 final String fieldName,
                                 final Class<T> fieldType){
        return getField(descr, fieldName, fieldType, (Supplier<T>) () -> null);
    }

    public static Properties toProperties(final Descriptor descr) {
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

    public static String toXML(final Descriptor descr, final String comments) throws IOException {
        final Properties props = toProperties(descr);
        final String ENCODING = "UTF-8";
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream(1024)) {
            props.storeToXML(out, comments, ENCODING);
            return new String(out.toByteArray(), ENCODING);
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
     * @return A map of fields.
     */
    public static Map<String, ?> toMap(final Descriptor descr, final boolean ignoreNullValues){
        return toMap(descr, Object.class, ignoreNullValues);
    }

    public static <V> Map<String, V> toMap(final Descriptor descr, final Class<V> valueType, final boolean ignoreNullValues) {
        if (descr == null) return Collections.emptyMap();
        final String[] fields = descr.getFieldNames();
        final Map<String, V> result = Maps.newHashMapWithExpectedSize(fields.length);
        for (final String fieldName : fields) {
            final V fieldValue = getField(descr, fieldName, valueType);
            if (fieldValue == null && ignoreNullValues) continue;
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
        return ArrayUtils.containsAny(descr.getFieldNames(), fieldName);
    }

    public static boolean hasDefaultValue(final Descriptor descr){
        return hasField(descr, DEFAULT_VALUE_FIELD);
    }

    public static boolean hasLegalValues(final Descriptor descr){
        return hasField(descr, LEGAL_VALUES_FIELD);
    }

    public static boolean hasMaxValue(final Descriptor descr){
        return hasField(descr, MAX_VALUE_FIELD);
    }

    public static boolean hasMinValue(final Descriptor descr){
        return hasField(descr, MIN_VALUE_FIELD);
    }

    public static <T> T getDefaultValue(final Descriptor descr, final Class<T> type){
        final Object value = descr.getFieldValue(DEFAULT_VALUE_FIELD);
        return type.isInstance(value) ? type.cast(value) : null;
    }

    public static Object getRawLegalValues(final Descriptor descr){
        return descr.getFieldValue(LEGAL_VALUES_FIELD);
    }

    public static Set<?> getLegalValues(final Descriptor descr){
        final Object value = getRawLegalValues(descr);
        return value instanceof Set<?> ? (Set<?>)value : null;
    }

    public static Object getRawMaxValue(final Descriptor descr){
        return descr.getFieldValue(MAX_VALUE_FIELD);
    }

    public static Comparable<?> getMaxValue(final Descriptor descr){
        final Object value = getRawMaxValue(descr);
        return value instanceof Comparable<?> ? (Comparable<?>)value : null;
    }

    public static Object getRawMinValue(final Descriptor descr){
        return descr.getFieldValue(MIN_VALUE_FIELD);
    }

    public static Comparable<?> getMinValue(final Descriptor descr){
        final Object value = getRawMinValue(descr);
        return value instanceof Comparable<?> ? (Comparable<?>)value : null;
    }

    public static String getUOM(final Descriptor descr){
        return getField(descr, UNIT_OF_MEASUREMENT_FIELD, String.class, "");
    }

    public static ImmutableDescriptor copyOf(final Descriptor descr){
        return new ImmutableDescriptor(toMap(descr));
    }
}
