package com.bytex.snamp.connector.md;

import javax.management.AttributeNotFoundException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class UnrecognizedAttributeTypeException extends AttributeNotFoundException {
    private static final long serialVersionUID = -9155555226277765316L;

    UnrecognizedAttributeTypeException(final String attributeName){
        super(String.format("Attribute '%s' is not recognized by MD connector", attributeName));
    }
}
