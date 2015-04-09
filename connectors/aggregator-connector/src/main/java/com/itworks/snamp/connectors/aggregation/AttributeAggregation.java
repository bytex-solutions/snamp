package com.itworks.snamp.connectors.aggregation;

import org.osgi.framework.BundleContext;

import javax.management.JMException;

/**
 * Represents aggregation of attribute values.
 * @param <V> Type of the aggregation result.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface AttributeAggregation<V> {
    V compute(final BundleContext context) throws JMException;

    /**
     * The name of the managed resource used as a source for attributes used in this aggregation.
     * @return The name of the managed resource.
     */
    String getResourceName();
}
