package com.bytex.snamp.adapters.groovy;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.modeling.AttributeAccessor;
import com.bytex.snamp.internal.RecordReader;

/**
 * Represents root-level DSL for working with attributes.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface AttributesRootAPI {
    <E extends Exception> void processAttributes(final RecordReader<String, AttributeAccessor, E> handler) throws E;
    ResourceAttributesAnalyzer<?> attributesAnalyzer(final TimeSpan checkPeriod);
}