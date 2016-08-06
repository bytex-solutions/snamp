package com.bytex.snamp.connectors;

import javax.management.MBeanFeatureInfo;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public abstract class FeatureModifiedEvent<F extends MBeanFeatureInfo> extends ResourceEvent {
    private static final long serialVersionUID = -4019967787362487363L;
    private final F feature;

    FeatureModifiedEvent(final Object sender,
                         final String resourceName,
                         final F feature) {
        super(sender, resourceName);
        this.feature = Objects.requireNonNull(feature);
    }


    public final F getFeature(){
        return feature;
    }
}
