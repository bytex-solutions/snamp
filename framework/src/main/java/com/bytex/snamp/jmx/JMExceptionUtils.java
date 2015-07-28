package com.bytex.snamp.jmx;

import javax.management.AttributeNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.NotificationListener;

/**
 * Provides various methods for working with JMX exceptions.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JMExceptionUtils {
    private JMExceptionUtils(){

    }

    public static AttributeNotFoundException attributeNotFound(final String attributeName){
        return new AttributeNotFoundException(String.format("Attribute %s doesn't exist.", attributeName));
    }

    public static ListenerNotFoundException listenerNotFound(final NotificationListener listener) {
        return new ListenerNotFoundException(String.format("Listener %s doesn't exist.", listener));
    }
}
