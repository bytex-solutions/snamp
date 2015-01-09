package com.itworks.snamp.adapters;

import com.itworks.snamp.core.RichLogicalOperation;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class AttributeRelatedLogicalOperation extends RichLogicalOperation {
    public static final String ATTRIBUTE_ID_PARAMETER = "attributeID";
    public static final String ATTRIBUTE_NAME_PARAMETER = "attributeName";

    protected AttributeRelatedLogicalOperation(final String operationName,
                                         final String attributeName,
                                         final String attributeID){
        super(operationName, ATTRIBUTE_ID_PARAMETER, attributeID,
                ATTRIBUTE_NAME_PARAMETER, attributeName);
    }

    public AttributeRelatedLogicalOperation(final String operationName,
                                         final String attributeName,
                                         final String attributeID,
                                         final String propertyName,
                                         final Object propertyValue){
        super(operationName, ATTRIBUTE_ID_PARAMETER, attributeID,
                ATTRIBUTE_NAME_PARAMETER, attributeName,
                propertyName, propertyValue);
    }

    public AttributeRelatedLogicalOperation(final String operationName,
                                         final String attributeName,
                                         final String attributeID,
                                         final String propertyName1,
                                         final Object propertyValue1,
                                         final String propertyName2,
                                         final Object propertyValue2){
        super(operationName, ATTRIBUTE_ID_PARAMETER, attributeID,
                ATTRIBUTE_NAME_PARAMETER, attributeName,
                propertyName1, propertyValue1,
                propertyName2, propertyValue2);
    }

    public final String getAttributeID(){
        return getProperty(ATTRIBUTE_ID_PARAMETER, String.class, "");
    }

    public final String getAttributeName(){
        return getProperty(ATTRIBUTE_NAME_PARAMETER, String.class, "");
    }
}
