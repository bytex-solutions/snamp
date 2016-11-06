package com.bytex.snamp.connector.operations.reflection;

import javax.management.MBeanOperationInfo;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks method as a management operation. Marked operation should be public non-static.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ManagementOperation {
    /**
     * Gets description of the management operation.
     * @return The description of the management operation.
     */
    String description() default "";

    /**
     * The impact of the method.
     * @return The impact of the method.
     * @see MBeanOperationInfo#UNKNOWN
     * @see MBeanOperationInfo#ACTION
     * @see MBeanOperationInfo#ACTION_INFO
     * @see MBeanOperationInfo#INFO
     */
    int impact() default MBeanOperationInfo.UNKNOWN;
}
