package com.bytex.snamp.jmx;

import javax.management.*;
import java.util.function.Function;

/**
 * Provides various methods for working with JMX exceptions.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class JMExceptionUtils {
    private JMExceptionUtils(){
        throw new InstantiationError();
    }

    public static <E extends JMException> E unreadableAttribute(final String attributeName, final Function<? super Exception, ? extends E> exceptionFactory){
        return exceptionFactory.apply(new IllegalArgumentException(String.format("Attribute %s is not available for reading", attributeName)));
    }

    public static <E extends JMException> E unwritableAttribute(final String attributeName, final Function<? super Exception, ? extends E> exceptionFactory){
        return exceptionFactory.apply(new IllegalArgumentException(String.format("Attribute %s is not available for writing", attributeName)));
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
