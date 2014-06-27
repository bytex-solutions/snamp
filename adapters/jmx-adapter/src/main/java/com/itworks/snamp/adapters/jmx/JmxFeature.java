package com.itworks.snamp.adapters.jmx;

import javax.management.MBeanFeatureInfo;
import javax.management.openmbean.OpenDataException;

/**
 * Represents a bridge between JMX technology and SNAMP.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface JmxFeature<F extends MBeanFeatureInfo> {
    F createFeature(final String featureName) throws OpenDataException;
}
