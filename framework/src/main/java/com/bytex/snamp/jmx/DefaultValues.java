package com.bytex.snamp.jmx;

import com.bytex.snamp.internal.Utils;
import com.google.common.collect.ImmutableMap;

import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.bytex.snamp.ArrayUtils.emptyArray;
import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * Represents a collection of default values for each possible OpenType.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class DefaultValues {
    private static final ImmutableMap<OpenType<?>, Object> values = Utils.interfaceStaticInitialize(() -> {
        final ImmutableMap.Builder<OpenType<?>, Object> builder = ImmutableMap.builder();
        //primitives
        put(builder, SimpleType.STRING, "");
        put(builder, SimpleType.BOOLEAN, false);
        put(builder, SimpleType.CHARACTER, '\0');
        put(builder, SimpleType.BYTE, (byte) 0);
        put(builder, SimpleType.SHORT, (short) 0);
        put(builder, SimpleType.INTEGER, 0);
        put(builder, SimpleType.LONG, 0L);
        put(builder, SimpleType.FLOAT, 0F);
        put(builder, SimpleType.DOUBLE, 0.0);
        put(builder, SimpleType.BIGINTEGER, BigInteger.ZERO);
        put(builder, SimpleType.BIGDECIMAL, BigDecimal.ZERO);
        put(builder, SimpleType.DATE, new Date(0L));
        put(builder, SimpleType.OBJECTNAME, new ObjectName(""));
        //arrays
        put(builder, new ArrayType<>(SimpleType.STRING, false));
        put(builder, new ArrayType<>(SimpleType.BOOLEAN, true));
        put(builder, new ArrayType<>(SimpleType.BOOLEAN, false));
        put(builder, new ArrayType<>(SimpleType.CHARACTER, true));
        put(builder, new ArrayType<>(SimpleType.CHARACTER, false));
        put(builder, new ArrayType<>(SimpleType.BYTE, true));
        put(builder, new ArrayType<>(SimpleType.BYTE, false));
        put(builder, new ArrayType<>(SimpleType.SHORT, true));
        put(builder, new ArrayType<>(SimpleType.SHORT, false));
        put(builder, new ArrayType<>(SimpleType.INTEGER, true));
        put(builder, new ArrayType<>(SimpleType.INTEGER, false));
        put(builder, new ArrayType<>(SimpleType.LONG, true));
        put(builder, new ArrayType<>(SimpleType.LONG, false));
        put(builder, new ArrayType<>(SimpleType.FLOAT, true));
        put(builder, new ArrayType<>(SimpleType.FLOAT, false));
        put(builder, new ArrayType<>(SimpleType.DOUBLE, true));
        put(builder, new ArrayType<>(SimpleType.DOUBLE, false));
        put(builder, new ArrayType<>(SimpleType.BIGINTEGER, false));
        put(builder, new ArrayType<>(SimpleType.BIGDECIMAL, false));
        put(builder, new ArrayType<>(SimpleType.DATE, false));
        put(builder, new ArrayType<>(SimpleType.OBJECTNAME, false));
        return builder.build();
    });

    //macros that provides type safety when put default value into map
    private static <T> void put(final ImmutableMap.Builder<OpenType<?>, Object> builder,
                                                                     final OpenType<T> type,
                                                                     final T value) {
        builder.put(type, value);
    }

    private static void put(final ImmutableMap.Builder<OpenType<?>, Object> builder,
                                final ArrayType<?> arrayType){
        builder.put(arrayType, emptyArray(arrayType, null));
    }

    private DefaultValues(){

    }

    /**
     * Gets default value of the specified OpenType.
     * @param type OpenType instance. Cannot be {@literal null}.
     * @param <T> Type of the value.
     * @return Default value of the specified type; or {@literal null}, if no default value is specified.
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(final OpenType<T> type) {
        if (type instanceof CompositeType) {
            final CompositeType ctype = (CompositeType) type;
            final Map<String, Object> items = ctype.keySet().stream()
                    .collect(Collectors.toMap(Function.identity(), itemName -> get(ctype.getType(itemName))));
            return callUnchecked(() -> (T) new CompositeDataSupport(ctype, items));
        } else return (T) values.get(type);
    }
}
