package com.itworks.snamp;

/**
 * Represents an exception occurred when type conversion is not possible.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ConversionException extends ClassCastException{
    /**
     * Represents value that cannot be converter to {@link #targetType}.
     */
    public final Object value;

    /**
     * Type of the conversion result.
     */
    public final Class<?> targetType;

    public ConversionException(final Object value, final Class<?> targetType){
        super(String.format("Unable convert %s value to %s type", value, targetType));
        this.value = value;
        this.targetType = targetType;
    }
}
