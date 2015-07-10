package com.itworks.snamp.adapters.groovy;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.adapters.AttributeAccessor;
import com.itworks.snamp.internal.RecordReader;
import groovy.lang.Closure;

import javax.management.JMException;

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
