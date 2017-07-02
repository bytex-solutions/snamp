package com.bytex.snamp.gateway.groovy.impl;

import com.bytex.snamp.gateway.groovy.ResourceAttributesAnalyzer;
import com.bytex.snamp.gateway.modeling.ModelOfAttributes;

import java.time.Duration;

/**
 * Represents script-based periodic analyzer of attributes.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class ScriptAttributesAnalyzer extends ResourceAttributesAnalyzer<ScriptAttributeAccessor> {
    /**
     * Initializes a new attribute value sender.
     *
     * @param period     Time between successive task executions. Cannot be {@literal null}.
     * @param attributes A collection of attributes. Cannot be {@literal null}.
     * @throws IllegalArgumentException period is {@literal null}.
     */
    ScriptAttributesAnalyzer(final Duration period, final ModelOfAttributes<ScriptAttributeAccessor> attributes) {
        super(period, attributes);
    }
}
