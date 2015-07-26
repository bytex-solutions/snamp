package com.bytex.snamp.connectors;

import javax.management.MBeanFeatureInfo;

/**
 * An event indicating that the managed resource is in process of deregistration
 * of the feature.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class FeatureRemovingEvent<F extends MBeanFeatureInfo> extends FeatureModifiedEvent<F> {
    private static final long serialVersionUID = -5222138982261493327L;

    public FeatureRemovingEvent(final Object sender, final String resourceName, final F feature) {
        super(sender, resourceName, feature);
    }
}
