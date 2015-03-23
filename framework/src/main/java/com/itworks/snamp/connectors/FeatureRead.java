package com.itworks.snamp.connectors;

import javax.management.MBeanFeatureInfo;

/**
 * Represents accessor to the managed resource feature.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface FeatureRead<F extends MBeanFeatureInfo> {
    /**
     * Gets feature of the managed resource.
     * @return The feature of the managed resource.
     */
    F getFeature();
}
