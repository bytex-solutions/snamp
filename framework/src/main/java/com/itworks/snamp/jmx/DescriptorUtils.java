package com.itworks.snamp.jmx;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.itworks.snamp.ArrayUtils;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import javax.management.JMX;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Represents utility methods for working with {@link javax.management.Descriptor} instances.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class DescriptorUtils {
    public static final String DEFAULT_VALUE_FIELD  = JMX.DEFAULT_VALUE_FIELD;
    public static final String LEGAL_VALUES_FIELD = JMX.LEGAL_VALUES_FIELD;
    public static final String MIN_VALUE_FIELD = JMX.MIN_VALUE_FIELD;
    public static final String MAX_VALUE_FIELD = JMX.MAX_VALUE_FIELD;
    public static final String UNIT_OF_MEASUREMENT_FIELD = "units";

    /**
     * Represents empty immutable descriptor.
     */
    public static final Descriptor EMPTY = new ImmutableDescriptor();

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
        return getField(descr, fieldName, fieldType, Suppliers.ofInstance(defval));
    }

    public static <T> T getField(final Descriptor descr,
                                 final String fieldName,
                                 final Class<T> fieldType){
        return getField(descr, fieldName, fieldType, Suppliers.<T>ofInstance(null));
    }

    public static Map<String, ?> toMap(final Descriptor descr){
        if(descr == null) return Collections.emptyMap();
        final String[] fields = descr.getFieldNames();
        final Map<String, Object> result = Maps.newHashMapWithExpectedSize(fields.length);
        for(final String fieldName: fields)
            result.put(fieldName, descr.getFieldValue(fieldName));
        return result;
    }

    public static boolean hasField(final Descriptor descr, final String fieldName){
        return ArrayUtils.contains(descr.getFieldNames(), fieldName);
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
