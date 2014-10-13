package com.itworks.snamp;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.reflect.Typed;

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
    public static final Typed<Character> CHAR = of(Character.class);

    /**
     * Represents wrapped {@link java.lang.Number}.
     */
    public static final Typed<Number> NUMBER = of(Number.class);

    /**
     * Represents wrapped {@link java.lang.Byte}.
     */
    public static final Typed<Byte> BYTE = of(Byte.class);

    /**
     * Represents wrapped {@link java.lang.Byte}[].
     */
    public static final Typed<Byte[]> BYTE_ARRAY = of(Byte[].class);

    /**
     * Represents wrapped {@link java.lang.Short}.
     */
    public static final Typed<Short> SHORT = of(Short.class);

    /**
     * Represents wrapped {@link java.lang.Integer}.
     */
    public static final Typed<Integer> INTEGER = of(Integer.class);

    /**
     * Represents wrapped {@link java.lang.Long}.
     */
    public static final Typed<Long> LONG = of(Long.class);

    /**
     * Represents wrapped {@link java.math.BigInteger}.
     */
    public static final Typed<BigInteger> BIG_INTEGER = of(BigInteger.class);

    /**
     * Represents wrapped {@link java.math.BigDecimal}.
     */
    public static final Typed<BigDecimal> BIG_DECIMAL = of(BigDecimal.class);

    /**
     * Represents wrapped {@link java.lang.Boolean}.
     */
    public static final Typed<Boolean> BOOLEAN = of(Boolean.class);

    /**
     * Represents wrapped {@link java.util.Date}.
     */
    public static final Typed<Date> DATE = of(Date.class);

    /**
     * Represents wrapped {@link java.lang.Float}.
     */
    public static final Typed<Float> FLOAT = of(Float.class);

    /**
     * Represents wrapped {@link java.lang.Double}.
     */
    public static final Typed<Double> DOUBLE = of(Double.class);

    /**
     * Represents wrapped {@link java.lang.String}.
     */
    public static final Typed<String> STRING = of(String.class);

    /**
     * Represents wrapped {@link java.lang.Object}.
     */
    public static final Typed<Object> OBJECT = of(Object.class);

    /**
     * Represents wrapped {@link java.lang.Object}[].
     */
    public static final Typed<Object[]> OBJECT_ARRAY = of(Object[].class);

    /**
     * Represents wrapped {@link com.itworks.snamp.Table}&lt;{@link java.lang.String}&gt;
     */
    public static final Typed<Table<String>> STRING_COLUMN_TABLE = new TypeLiteral<Table<String>>() {};

    /**
     * Represents wrapped {@link java.util.Map}&lt;{@link java.lang.String}, {@link java.lang.Object}&gt;
     */
    public static final Typed<Map<String, Object>> STRING_MAP = new TypeLiteral<Map<String, Object>>() {};

    /**
     * Represents wrapped {@link java.util.Calendar}.
     */
    public static final Typed<Calendar> CALENDAR = of(Calendar.class);

    public static <T> Typed<T> of(final Class<T> t) {
        return TypeUtils.wrap(t);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(final Object value, final Typed<T> target) throws ClassCastException {
        if (isInstance(value, target))
            return (T) value;
        else throw new ClassCastException(String.format("Unable to cast %s to %s", value, target.getType()));
    }

    @SuppressWarnings("unchecked")
    public static <T> T safeCast(final Object value, final Typed<T> target){
        return TypeUtils.isInstance(value, target.getType()) ? (T)value : null;
    }

    public static boolean isInstance(final Object value, final Typed<?> target){
        return TypeUtils.isInstance(value, target.getType());
    }

    public static boolean isAssignable(final Typed<?> type, final Typed<?> toType){
        return TypeUtils.isAssignable(type.getType(), toType.getType());
    }
}
