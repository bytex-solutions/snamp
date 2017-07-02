package com.bytex.snamp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the method, field, constructor or parameter as an element with special use.
 * This means that the program element is used by Reflection or native code
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.SOURCE)
public @interface SpecialUse {
    /**
     * Represents use case of the annotated element.
     * @since 2.0
     */
    enum Case{
        /**
         * Indicates that the annotated element is used indirectly by reflection.
         */
        REFLECTION,

        /**
         * Indicates that the annotated element is used indirectly by JVM-compliant script (JavaScript, Groovy etc.)
         */
        SCRIPTING,

        /**
         * Indicates that the annotated element is used indirectly by OSGi container (such constructor of {@link org.osgi.framework.BundleActivator}).
         */
        OSGi,

        /**
         * Indicates that the annotated element is used indirectly by JVM itself (such as field updater etc.).
         */
        JVM,

        /**
         * Indicates that the annotated element is used indirectly by serialization framework (JAXB, Jackson etc.)
         */
        SERIALIZATION
    }

    Case[] value();
}
