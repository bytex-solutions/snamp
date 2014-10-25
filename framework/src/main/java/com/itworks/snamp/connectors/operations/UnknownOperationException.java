package com.itworks.snamp.connectors.operations;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class UnknownOperationException extends Exception {
    public final String operationName;

    public UnknownOperationException(final String operationName){
        super(String.format("Unknown operation %s", operationName));
        this.operationName = operationName;
    }
}
