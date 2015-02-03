package com.itworks.snamp.jmx;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Collections2;
import com.itworks.snamp.ArrayUtils;

import javax.management.openmbean.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

/**
 * Provides helper methods that allows to create and
 * manipulate of {@link javax.management.openmbean.CompositeData} instances.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class CompositeDataUtils {
    private CompositeDataUtils() {

    }

    public static CompositeData create(final String typeName,
                                       final String description,
                                       final Map<String, ?> map,
                                       final Function<? super String, ? extends OpenType<?>> typeProvider,
                                       final Function<String, String> descriptionProvider) throws OpenDataException {
        final String[] itemNames = ArrayUtils.toArray(map.keySet(), String.class);
        final String[] itemDescriptions = ArrayUtils.toArray(
                Collections2.transform(map.keySet(), descriptionProvider),
                String.class);
        final OpenType<?>[] itemTypes = new OpenType<?>[itemNames.length];
        for (int i = 0; i < itemTypes.length; i++)
            itemTypes[i] = typeProvider.apply(itemNames[i]);
        return new CompositeDataSupport(new CompositeType(typeName,
                description,
                itemNames,
                itemDescriptions,
                itemTypes),
                map);
    }

    public static CompositeData create(final String typeName,
                                       final String description,
                                       final Map<String, ?> map,
                                       final Map<String, ? extends OpenType<?>> types,
                                       final Function<String, String> descriptionProvider) throws OpenDataException {
        return create(typeName, description, map, new Function<String, OpenType<?>>() {
            @Override
            public OpenType<?> apply(final String input) {
                return types.get(input);
            }
        }, descriptionProvider);
    }

    public static <V> CompositeData create(final String typeName,
                                           final String description,
                                           final Map<String, V> map,
                                           final OpenType<V> type) throws OpenDataException {
        final Function<? super String, ? extends OpenType<?>> typeProvider = Functions.constant(type);
        final Function<String, String> itemDescriptionProvider = Functions.identity();
        return create(typeName,
                description,
                map,
                typeProvider,
                itemDescriptionProvider);
    }

    public static <V> CompositeData create(final Map<String, V> map,
                                           final OpenType<V> type) throws OpenDataException {
        return create(map.getClass().getName(),
                map.toString(),
                map,
                type);
    }

    public static <T> T getValue(final CompositeData dict,
                            final String itemName,
                            final Class<T> itemType,
                            final Supplier<T> defval) {
        if (dict.containsKey(itemName)) {
            final Object result = dict.get(itemName);
            if (itemType.isInstance(result)) return itemType.cast(result);
        }
        return defval.get();
    }

    public static <T> T getValue(final CompositeData dict,
                            final String itemName,
                            final Class<T> itemType,
                            final T defval){
        return getValue(dict, itemName, itemType, Suppliers.ofInstance(defval));
    }

    public static <T> T getValue(final CompositeData dict,
                            final String itemName,
                            final Class<T> itemType) {
        return getValue(dict, itemName, itemType, Suppliers.<T>ofInstance(null));
    }

    public static boolean getBoolean(final CompositeData dict,
                                     final String itemName,
                                     final boolean defval){
        return getValue(dict, itemName, Boolean.class, defval);
    }

    public static byte getByte(final CompositeData dict,
                                     final String itemName,
                                     final byte defval){
        return getValue(dict, itemName, Byte.class, defval);
    }

    public static char getCharacter(final CompositeData dict,
                                     final String itemName,
                                     final char defval){
        return getValue(dict, itemName, Character.class, defval);
    }

    public static short getShort(final CompositeData dict,
                                     final String itemName,
                                     final short defval){
        return getValue(dict, itemName, Short.class, defval);
    }

    public static int getInteger(final CompositeData dict,
                                     final String itemName,
                                     final int defval){
        return getValue(dict, itemName, Integer.class, defval);
    }

    public static long getLong(final CompositeData dict,
                                     final String itemName,
                                     final long defval){
        return getValue(dict, itemName, Long.class, defval);
    }

    public static float getFloat(final CompositeData dict,
                                     final String itemName,
                                     final float defval){
        return getValue(dict, itemName, Float.class, defval);
    }

    public static double getDouble(final CompositeData dict,
                                     final String itemName,
                                     final double defval){
        return getValue(dict, itemName, Double.class, defval);
    }

    public static BigInteger getBigInteger(final CompositeData dict,
                                     final String itemName,
                                     final BigInteger defval){
        return getValue(dict, itemName, BigInteger.class, defval);
    }

    public static BigDecimal getBigDecimal(final CompositeData dict,
                                     final String itemName,
                                     final BigDecimal defval){
        return getValue(dict, itemName, BigDecimal.class, defval);
    }

    public static String getString(final CompositeData dict,
                                     final String itemName,
                                     final String defval){
        return getValue(dict, itemName, String.class, defval);
    }

    public static Date getDate(final CompositeData dict,
                                     final String itemName,
                                     final Date defval){
        return getValue(dict, itemName, Date.class, defval);
    }
}