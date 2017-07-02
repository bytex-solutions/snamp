package com.bytex.snamp.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a set of Maven dependencies used to resolve required features.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MavenDependencies {
    MavenArtifact[] bundles() default {};
    MavenFeature[] features() default {};
}
