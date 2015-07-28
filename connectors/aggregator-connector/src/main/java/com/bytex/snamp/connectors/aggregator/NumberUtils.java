package com.bytex.snamp.connectors.aggregator;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NumberUtils {
    private NumberUtils(){

    }

    static BigDecimal toBigDecimal(final Object value) throws NumberFormatException{
        if(value instanceof BigDecimal)
            return (BigDecimal)value;
        else if(value instanceof BigInteger)
            return new BigDecimal((BigInteger)value);
        else if(value instanceof Long)
            return BigDecimal.valueOf((Long)value);
        else if(value instanceof Integer)
            return BigDecimal.valueOf((Integer)value);
        else if(value instanceof Number)
            return BigDecimal.valueOf(((Number)value).doubleValue());
        else if(value instanceof String)
            return new BigDecimal((String)value);
        else if(value instanceof Boolean)
            return ((Boolean)value) ? BigDecimal.ONE : BigDecimal.ZERO;
        else throw new NumberFormatException(String.format("Value %s is not a number", value));
    }

    static long toLong(final Object value) throws NumberFormatException{
        if(value instanceof Number)
            return ((Number)value).longValue();
        else if(value instanceof String)
            return Long.parseLong((String)value);
        else if(value instanceof Boolean)
            return ((Boolean)value) ? 1L : 0L;
        else throw new NumberFormatException(String.format("Value %s is not a number", value));
    }
}
