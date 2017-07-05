package com.bytex.snamp.jmx;

import com.bytex.snamp.internal.Utils;
import com.google.common.collect.ImmutableMap;

import javax.management.ObjectName;
import javax.management.openmbean.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
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
    private static final ImmutableMap<OpenType<?>, Object> values = Utils.staticInit(() -> {
        final Map<OpenType<?>, Object> types = new HashMap<>();
        //primitives
        simpleType(types, SimpleType.STRING, "");
        simpleType(types, SimpleType.BOOLEAN, false);
        simpleType(types, SimpleType.CHARACTER, '\0');
        simpleType(types, SimpleType.BYTE, (byte) 0);
        simpleType(types, SimpleType.SHORT, (short) 0);
        simpleType(types, SimpleType.INTEGER, 0);
        simpleType(types, SimpleType.LONG, 0L);
        simpleType(types, SimpleType.FLOAT, 0F);
        simpleType(types, SimpleType.DOUBLE, 0.0);
        simpleType(types, SimpleType.BIGINTEGER, BigInteger.ZERO);
        simpleType(types, SimpleType.BIGDECIMAL, BigDecimal.ZERO);
        simpleType(types, SimpleType.DATE, new Date(0L));
        simpleType(types, SimpleType.OBJECTNAME, new ObjectName(""));
        //arrays
        arrayType(types, new ArrayType<>(SimpleType.STRING, false));
        arrayType(types, new ArrayType<>(SimpleType.BOOLEAN, true));
        arrayType(types, new ArrayType<>(SimpleType.BOOLEAN, false));
        arrayType(types, new ArrayType<>(SimpleType.CHARACTER, true));
        arrayType(types, new ArrayType<>(SimpleType.CHARACTER, false));
        arrayType(types, new ArrayType<>(SimpleType.BYTE, true));
        arrayType(types, new ArrayType<>(SimpleType.BYTE, false));
        arrayType(types, new ArrayType<>(SimpleType.SHORT, true));
        arrayType(types, new ArrayType<>(SimpleType.SHORT, false));
        arrayType(types, new ArrayType<>(SimpleType.INTEGER, true));
        arrayType(types, new ArrayType<>(SimpleType.INTEGER, false));
        arrayType(types, new ArrayType<>(SimpleType.LONG, true));
        arrayType(types, new ArrayType<>(SimpleType.LONG, false));
        arrayType(types, new ArrayType<>(SimpleType.FLOAT, true));
        arrayType(types, new ArrayType<>(SimpleType.FLOAT, false));
        arrayType(types, new ArrayType<>(SimpleType.DOUBLE, true));
        arrayType(types, new ArrayType<>(SimpleType.DOUBLE, false));
        arrayType(types, new ArrayType<>(SimpleType.BIGINTEGER, false));
        arrayType(types, new ArrayType<>(SimpleType.BIGDECIMAL, false));
        arrayType(types, new ArrayType<>(SimpleType.DATE, false));
        arrayType(types, new ArrayType<>(SimpleType.OBJECTNAME, false));
        return ImmutableMap.copyOf(types);
    });

    //macros that provides type safety when put default value into map
    private static <T> void simpleType(final Map<OpenType<?>, Object> builder,
                                       final SimpleType<T> type,
                                       final T value) {
        builder.put(type, value);
    }

    private static void arrayType(final Map<OpenType<?>, Object> builder,
                                  final ArrayType<?> arrayType){
        builder.put(arrayType, emptyArray(arrayType, null));
    }

    private DefaultValues(){
        throw new InstantiationError();
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
