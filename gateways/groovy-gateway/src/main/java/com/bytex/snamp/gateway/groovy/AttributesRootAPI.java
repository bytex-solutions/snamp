package com.bytex.snamp.gateway.groovy;

import com.bytex.snamp.gateway.modeling.AttributeAccessor;
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
