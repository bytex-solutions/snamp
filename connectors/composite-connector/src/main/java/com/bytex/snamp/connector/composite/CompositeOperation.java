package com.bytex.snamp.connector.composite;

import com.bytex.snamp.Box;

import javax.management.MBeanOperationInfo;

/**
 * Represents operation as a part of composition.
 */
final class CompositeOperation extends MBeanOperationInfo implements CompositeFeature {
    private final String connectorType;
    private final String shortName;

    CompositeOperation(final String operationName, final MBeanOperationInfo operation){
        super(operationName, operation.getDescription(), operation.getSignature(), operation.getReturnType(), operation.getImpact(), operation.getDescriptor());
        final Box<String> connectorType = new Box<>();
        final Box<String> shortName = new Box<>();
        if(ConnectorTypeSplit.split(operationName, connectorType, shortName)){
            this.connectorType = connectorType.get();
            this.shortName = shortName.get();
        } else
            throw invalidName(operationName);
    }

    static IllegalArgumentException invalidName(final String operationName){
        return new IllegalArgumentException("Invalid operation name: " + operationName);
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public String getConnectorType() {
        return connectorType;
    }
}
