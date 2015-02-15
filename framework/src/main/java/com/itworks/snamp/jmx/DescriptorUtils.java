package com.itworks.snamp.jmx;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import com.itworks.snamp.ArrayUtils;

import javax.management.Descriptor;
import javax.management.ImmutableDescriptor;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Map;

/**
 * Represents utility methods for working with {@link javax.management.Descriptor} instances.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class DescriptorUtils {
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

    public static ImmutableDescriptor copyOf(final Descriptor descr){
        return new ImmutableDescriptor(toMap(descr));
    }
}
