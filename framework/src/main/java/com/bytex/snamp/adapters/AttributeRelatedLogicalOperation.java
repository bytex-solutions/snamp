package com.bytex.snamp.adapters;

import com.bytex.snamp.core.RichLogicalOperation;
import com.google.common.collect.ImmutableMap;

import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class AttributeRelatedLogicalOperation extends RichLogicalOperation {
    public static final String ATTRIBUTE_ID_PARAMETER = "attributeID";
    public static final String ATTRIBUTE_NAME_PARAMETER = "attributeName";

    protected AttributeRelatedLogicalOperation(final Logger logger,
                                               final String operationName,
                                         final String attributeName,
                                         final String attributeID){
        super(logger, operationName, ImmutableMap.of(ATTRIBUTE_ID_PARAMETER, attributeID,
                ATTRIBUTE_NAME_PARAMETER, attributeName));
    }

    public AttributeRelatedLogicalOperation(final Logger logger,
                                            final String operationName,
                                         final String attributeName,
                                         final String attributeID,
                                         final String propertyName,
                                         final Object propertyValue){
        super(logger, operationName, ImmutableMap.of(ATTRIBUTE_ID_PARAMETER, attributeID,
                ATTRIBUTE_NAME_PARAMETER, attributeName,
                propertyName, propertyValue));
    }

    public final String getAttributeID(){
        return getProperty(ATTRIBUTE_ID_PARAMETER, String.class, "");
    }

    public final String getAttributeName(){
        return getProperty(ATTRIBUTE_NAME_PARAMETER, String.class, "");
    }
}
