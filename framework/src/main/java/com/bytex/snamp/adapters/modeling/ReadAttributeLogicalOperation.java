package com.bytex.snamp.adapters.modeling;

import com.bytex.snamp.adapters.AttributeRelatedLogicalOperation;

import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class ReadAttributeLogicalOperation extends AttributeRelatedLogicalOperation {
    public static final String OPERATION_NAME = "readAttribute";

    public ReadAttributeLogicalOperation(final Logger logger,
                                         final String attributeName,
                                         final String attributeID){
        super(logger, OPERATION_NAME, attributeName, attributeID);
    }

    public ReadAttributeLogicalOperation(final Logger logger,
                                         final String attributeName,
                                         final String attributeID,
                                         final String propertyName,
                                         final Object propertyValue){
        super(logger, OPERATION_NAME, attributeName, attributeID,
                propertyName, propertyValue);
    }
}