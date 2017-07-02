package com.bytex.snamp.gateway;

import com.bytex.snamp.core.LoggingScope;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class AttributeRelatedLoggingScope extends LoggingScope {
    public final String attributeName;
    public final String attributeID;

    public AttributeRelatedLoggingScope(final Object requester,
                                           final String operationName,
                                           final String attributeName,
                                           final String attributeID){
        super(requester, operationName);
        this.attributeID = attributeID;
        this.attributeName = attributeName;
    }
}
