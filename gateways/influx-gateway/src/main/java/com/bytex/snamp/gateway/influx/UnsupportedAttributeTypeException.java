package com.bytex.snamp.gateway.influx;

import javax.management.InvalidAttributeValueException;

/**
 * Indicates that the attribute cannot be converted into measurement point.
 * @since 2.0
 * @version 2.0
 */
final class UnsupportedAttributeTypeException extends InvalidAttributeValueException {
    private static final long serialVersionUID = -5024532506793495367L;
    private final Class<?> unsupportedType;

    UnsupportedAttributeTypeException(final Class<?> type){
        super(String.format("Type %s cannot be converted into measurement point", type));
        unsupportedType = type;
    }

    Class<?> getUnsupportedType(){
        return unsupportedType;
    }
}
