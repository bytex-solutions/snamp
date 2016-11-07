package com.bytex.snamp.jmx;

import javax.management.AttributeNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.NotificationListener;
import javax.management.OperationsException;

/**
 * Provides various methods for working with JMX exceptions.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class JMExceptionUtils {
    private JMExceptionUtils(){
        throw new InstantiationError();
    }

    public static AttributeNotFoundException attributeNotFound(final String attributeName){
        return new AttributeNotFoundException(String.format("Attribute %s doesn't exist.", attributeName));
    }

    public static OperationsException operationNotFound(final String operationName){
        return new OperationsException(String.format("Operation %s doesn't exist.", operationName));
    }

    public static OperationsException operationDisconnected(final String operationName){
        return new OperationsException(String.format("Operation %s is not connected to any resource.", operationName));
    }

    public static ListenerNotFoundException listenerNotFound(final NotificationListener listener) {
        return new ListenerNotFoundException(String.format("Listener %s doesn't exist.", listener));
    }
}
