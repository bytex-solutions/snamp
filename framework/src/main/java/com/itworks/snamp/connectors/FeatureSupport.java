package com.itworks.snamp.connectors;

import javax.management.MBeanFeatureInfo;

/**
 * Represents a root interface that describes accessor to the managed resource
 * features.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface FeatureSupport {
    /**
     * Gets name of the resource.
     * @return The name of the resource.
     */
    String getResourceName();

    /**
     * Returns an array of all supported resource features.
     * @return An array of all supported resource features.
     */
    MBeanFeatureInfo[] getFeatureInfo();
}
