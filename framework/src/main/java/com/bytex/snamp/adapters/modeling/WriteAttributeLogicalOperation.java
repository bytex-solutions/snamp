package com.bytex.snamp.adapters.modeling;

import com.bytex.snamp.adapters.AttributeRelatedLogicalOperation;

import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class WriteAttributeLogicalOperation extends AttributeRelatedLogicalOperation {
    public static final String OPERATION_NAME = "writeAttribute";

    public WriteAttributeLogicalOperation(final Logger logger,
                                          final String attributeName,
                                          final String attributeID) {
        super(logger, OPERATION_NAME, attributeName, attributeID);
    }

    public WriteAttributeLogicalOperation(final Logger logger,
                                          final String attributeName,
                                          final String attributeID,
                                          final String propertyName,
                                          final Object propertyValue) {
        super(logger, OPERATION_NAME, attributeName, attributeID,
                propertyName, propertyValue);
    }
}