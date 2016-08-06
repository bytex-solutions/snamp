package com.bytex.snamp.adapters.groovy;

import com.bytex.snamp.adapters.modeling.AttributeAccessor;
import com.bytex.snamp.EntryReader;

import java.time.Duration;

/**
 * Represents root-level DSL for working with attributes.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface AttributesRootAPI {
    <E extends Exception> void processAttributes(final EntryReader<String, AttributeAccessor, E> handler) throws E;
    ResourceAttributesAnalyzer<?> attributesAnalyzer(final Duration checkPeriod);
}
