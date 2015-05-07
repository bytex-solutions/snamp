package com.itworks.snamp.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Instructs integration test engine to propagate system property from host process
 * to test container. This annotation should be applied to {@link com.itworks.snamp.testing.AbstractIntegrationTest}
 * derivatives.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PropagateSystemProperties {

    /**
     * Gets an array of system properties to be propagated in test container
     * @return An array of system properties to be propagated in test container
     */
    String[] value() default {};
}
