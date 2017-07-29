package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.gateway.AttributeRelatedLoggingScope;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public class ReadAttributeLoggingScope extends AttributeRelatedLoggingScope {
    private static final String OPERATION_NAME = "readAttribute";

    public ReadAttributeLoggingScope(final Object requester,
                                     final String attributeName,
                                     final String attributeID){
        super(requester, OPERATION_NAME, attributeName, attributeID);
    }
}