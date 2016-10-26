package com.bytex.snamp.jmx;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.SimpleType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Parses string to one of the JMX Open simple types.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 * @see javax.management.openmbean.SimpleType
 */
public final class SimpleTypeParser {
    private DateFormat dateFormat;
    private DecimalFormat numberFormat;

    public SimpleTypeParser(){
        dateFormat = new SimpleDateFormat();
        numberFormat = new DecimalFormat();
    }

    public void setDateFormat(final DateFormat value){
        dateFormat = Objects.requireNonNull(value);
    }

    public void setDecimalFormat(final DecimalFormat format){
        numberFormat = Objects.requireNonNull(format);
    }

    public Comparable<?> parse(final WellKnownType type, final String value) throws SimpleTypeParseException{
        try {
            switch (type) {
                case BIG_DECIMAL:
                    return new BigDecimal(value);
                case BIG_INT:
                    return new BigInteger(value);
                case BYTE:
                    return numberFormat.parse(value).byteValue();
                case SHORT:
                    return numberFormat.parse(value).shortValue();
                case INT:
                    return numberFormat.parse(value).intValue();
                case LONG:
                    return numberFormat.parse(value).longValue();
                case FLOAT:
                    return numberFormat.parse(value).floatValue();
                case DOUBLE:
                    return numberFormat.parse(value).doubleValue();
                case BOOL:
                    return Boolean.parseBoolean(value);
                case STRING:
                    return value;
                case OBJECT_NAME:
                    return new ObjectName(value);
                case VOID:
                    return null;
                case CHAR:
                    return value == null || value.isEmpty() ? '\0' : value.charAt(0);
                case DATE:
                    return dateFormat.parse(value);
                default:
                    throw new ParseException(value, 0);
            }
        }
        catch (final NumberFormatException | MalformedObjectNameException | ParseException e){
            throw new SimpleTypeParseException(value, e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T parse(final SimpleType<T> type, final String value) throws SimpleTypeParseException {
        return (T)parse(WellKnownType.getType(type), value);
    }

    public <T> String toString(final SimpleType<T> type, final T value){
        if(type == null) throw new IllegalArgumentException("type is null");
        else if(value instanceof Number)
            return numberFormat.format(value);
        else if(value instanceof Date)
            return dateFormat.format(value);
        else if(value != null)
            return value.toString();
        else return null;
    }
}
