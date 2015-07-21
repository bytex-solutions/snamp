package com.itworks.snamp.adapters.groovy.impl;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.modeling.ModelOfAttributes;
import com.itworks.snamp.adapters.groovy.ResourceAttributesAnalyzer;

/**
 * Represents script-based periodic analyzer of attributes.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
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
    ScriptAttributesAnalyzer(final TimeSpan period, final ModelOfAttributes<ScriptAttributeAccessor> attributes) {
        super(period, attributes);
    }
}
