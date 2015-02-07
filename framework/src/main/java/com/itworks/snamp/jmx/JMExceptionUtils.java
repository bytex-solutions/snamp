package com.itworks.snamp.jmx;

import javax.management.AttributeNotFoundException;

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
        return new AttributeNotFoundException(String.format("Attribute %s doesn't exist."));
    }
}
