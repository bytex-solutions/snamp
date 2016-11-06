package com.bytex.snamp.connector.operations.reflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Associates additional information with parameter of the management operation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface OperationParameter {
    /**
     * Gets description of the parameter.
     * @return The description of the parameter.
     */
    String description() default "";

    /**
     * Gets name of the parameter.
     * @return The name of the parameter.
     */
    String name();
}
