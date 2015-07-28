package com.bytex.snamp.adapters.modeling;

import com.bytex.snamp.adapters.AttributeRelatedLogicalOperation;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ReadAttributeLogicalOperation extends AttributeRelatedLogicalOperation {
    public static final String OPERATION_NAME = "readAttribute";

    public ReadAttributeLogicalOperation(final String attributeName,
                                         final String attributeID){
        super(OPERATION_NAME, attributeName, attributeID);
    }

    public ReadAttributeLogicalOperation(final String attributeName,
                                         final String attributeID,
                                         final String propertyName,
                                         final Object propertyValue){
        super(OPERATION_NAME, attributeName, attributeID,
                propertyName, propertyValue);
    }

    public ReadAttributeLogicalOperation(final String attributeName,
                                         final String attributeID,
                                         final String propertyName1,
                                         final Object propertyValue1,
                                         final String propertyName2,
                                         final Object propertyValue2){
        super(OPERATION_NAME, attributeID, attributeName,
                propertyName1, propertyValue1,
                propertyName2, propertyValue2);
    }
}