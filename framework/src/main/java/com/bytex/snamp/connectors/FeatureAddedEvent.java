package com.bytex.snamp.connectors;

import javax.management.MBeanFeatureInfo;

/**
 * An event that describes a new feature supported by the managed resource.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class FeatureAddedEvent<F extends MBeanFeatureInfo> extends FeatureModifiedEvent<F> {
    private static final long serialVersionUID = -3948322741474698012L;

    public FeatureAddedEvent(final Object sender,
                             final String resourceName,
                             final F feature) {
        super(sender, resourceName, feature);
    }
}
