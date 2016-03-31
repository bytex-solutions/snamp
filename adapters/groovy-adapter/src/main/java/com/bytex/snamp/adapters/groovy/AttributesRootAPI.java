package com.bytex.snamp.adapters.groovy;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.modeling.AttributeAccessor;
import com.bytex.snamp.EntryReader;

/**
 * Represents root-level DSL for working with attributes.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.2
 */
public interface AttributesRootAPI {
    <E extends Exception> void processAttributes(final EntryReader<String, AttributeAccessor, E> handler) throws E;
    ResourceAttributesAnalyzer<?> attributesAnalyzer(final TimeSpan checkPeriod);
}
