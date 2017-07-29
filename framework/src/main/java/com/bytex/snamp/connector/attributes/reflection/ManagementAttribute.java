package com.bytex.snamp.connector.attributes.reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks getter or setter as a management attribute.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ManagementAttribute {
    /**
     * Determines whether the attribute if cached.
     * @return {@literal true}, if attribute value is cached in the private field; otherwise, {@literal false}.
     */
    boolean cached() default false;

    /**
     * Gets the description of the attribute.
     * @return The description of the attribute.
     */
    String description() default "";

    /**
     * Represents attribute marshaller that is used to convert custom Java type to
     * JMX-compliant value and vice versa.
     * @return The attribute formatter.
     */
    Class<? extends ManagementAttributeMarshaller<?>> marshaller() default DefaultManagementAttributeMarshaller.class;
}
