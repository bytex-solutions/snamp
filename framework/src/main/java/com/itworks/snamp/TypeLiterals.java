package com.itworks.snamp;

import com.google.common.collect.Table;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Represents a set of predefined literals.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class TypeLiterals {
    private TypeLiterals(){
    }

    /**
     * Represents wrapped {@link java.lang.Character}.
     */
    public static final TypeToken<Character> CHAR = TypeToken.of(Character.class);

    /**
     * Represents wrapped {@link java.lang.Number}.
     */
    public static final TypeToken<Number> NUMBER = TypeToken.of(Number.class);

    /**
     * Represents wrapped {@link java.lang.Byte}.
     */
    public static final TypeToken<Byte> BYTE = TypeToken.of(Byte.class);

    /**
     * Represents wrapped {@link java.lang.Byte}[].
     */
    public static final TypeToken<Byte[]> BYTE_ARRAY = TypeToken.of(Byte[].class);

    /**
     * Represents wrapped {@link java.lang.Short}.
     */
    public static final TypeToken<Short> SHORT = TypeToken.of(Short.class);

    /**
     * Represents wrapped {@link java.lang.Integer}.
     */
    public static final TypeToken<Integer> INTEGER = TypeToken.of(Integer.class);

    /**
     * Represents wrapped {@link java.lang.Long}.
     */
    public static final TypeToken<Long> LONG = TypeToken.of(Long.class);

    /**
     * Represents wrapped {@link java.math.BigInteger}.
     */
    public static final TypeToken<BigInteger> BIG_INTEGER = TypeToken.of(BigInteger.class);

    /**
     * Represents wrapped {@link java.math.BigDecimal}.
     */
    public static final TypeToken<BigDecimal> BIG_DECIMAL = TypeToken.of(BigDecimal.class);

    /**
     * Represents wrapped {@link java.lang.Boolean}.
     */
    public static final TypeToken<Boolean> BOOLEAN = TypeToken.of(Boolean.class);

    /**
     * Represents wrapped {@link java.util.Date}.
     */
    public static final TypeToken<Date> DATE = TypeToken.of(Date.class);

    /**
     * Represents wrapped {@link java.lang.Float}.
     */
    public static final TypeToken<Float> FLOAT = TypeToken.of(Float.class);

    /**
     * Represents wrapped {@link java.lang.Double}.
     */
    public static final TypeToken<Double> DOUBLE = TypeToken.of(Double.class);

    /**
     * Represents wrapped {@link java.lang.String}.
     */
    public static final TypeToken<String> STRING = TypeToken.of(String.class);

    /**
     * Represents wrapped {@link java.lang.Object}.
     */
    public static final TypeToken<Object> OBJECT = TypeToken.of(Object.class);

    /**
     * Represents wrapped {@link java.lang.Object}[].
     */
    public static final TypeToken<Object[]> OBJECT_ARRAY = TypeToken.of(Object[].class);

    /**
     * Represents wrapped {@link com.google.common.collect.Table}&lt;{@link java.lang.Integer}, {@link java.lang.String}, {@link java.lang.Object}&gt;
     */
    public static final TypeToken<Table<Integer, String, Object>> STRING_COLUMN_TABLE = new TypeToken<Table<Integer, String, Object>>() {};

    /**
     * Represents wrapped {@link java.util.Map}&lt;{@link java.lang.String}, {@link java.lang.Object}&gt;
     */
    public static final TypeToken<Map<String, Object>> STRING_MAP = new TypeToken<Map<String, Object>>() {};

    /**
     * Represents wrapped {@link java.util.Calendar}.
     */
    public static final TypeToken<Calendar> CALENDAR = TypeToken.of(Calendar.class);


    @SuppressWarnings("unchecked")
    public static <T> T cast(final Object value, final TypeToken<T> target) throws ClassCastException {
        if (isInstance(value, target))
            return (T) value;
        else throw new ClassCastException(String.format("Unable to cast %s to %s", value, target));
    }

    @SuppressWarnings("unchecked")
    public static <T> T safeCast(final Object value, final TypeToken<T> target){
        return isInstance(value, target) ? (T)value : null;
    }

    public static boolean isInstance(final Object value, final TypeToken<?> target) {
        return value != null && target.isAssignableFrom(value.getClass());
    }

    public static boolean isAssignable(final Type from, final Type to) {
        if (to == null) return from == null;
        else if (to instanceof TypeToken<?>)
            return ((TypeToken<?>) to).isAssignableFrom(from);
        else if (to instanceof Class<?> && from instanceof Class<?>)
            return ((Class<?>) to).isAssignableFrom((Class<?>) from);
        else return TypeToken.of(to).isAssignableFrom(from);
    }
}
