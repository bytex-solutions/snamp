package com.bytex.snamp.testing;

/**
 * Represents Karaf feature located in Maven Central repository.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public @interface MavenFeature {
    String groupId();
    String artifactId();
    String version();
    String name();
}
