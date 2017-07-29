package com.bytex.snamp.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents Karaf feature located in Maven Central repository.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MavenArtifact {
    String groupId();
    String artifactId();
    String version();
}
