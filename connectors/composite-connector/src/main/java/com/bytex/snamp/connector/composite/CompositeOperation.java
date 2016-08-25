package com.bytex.snamp.connector.composite;

import javax.management.MBeanOperationInfo;
import java.util.Objects;

/**
 * Represents operation as a part of composition.
 */
final class CompositeOperation extends MBeanOperationInfo implements CompositeFeature {
    private static final long serialVersionUID = -1323239811164296145L;
    private final String connectorType;

    CompositeOperation(final String connectorType, final MBeanOperationInfo operation){
        super(operation.getName(), operation.getDescription(), operation.getSignature(), operation.getReturnType(), operation.getImpact(), operation.getDescriptor());
        this.connectorType = Objects.requireNonNull(connectorType);
    }

    @Override
    public String getConnectorType() {
        return connectorType;
    }
}
