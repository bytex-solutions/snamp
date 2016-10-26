package com.bytex.snamp.connector.md;

import com.bytex.snamp.parser.ParseException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class UnrecognizedAttributeTypeException extends ParseException {
    private static final long serialVersionUID = -9155555226277765316L;

    UnrecognizedAttributeTypeException(final String attributeName){
        super(String.format("Attribute '%s' is not recognized by MD connector", attributeName));
    }
}
