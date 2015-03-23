package com.itworks.snamp.connectors;

import javax.management.MBeanFeatureInfo;
import java.util.Objects;

/**
 * Represents an event raised when managed resource connector was extended
 * with a new feature.
 * @param <F> Type of the feature added to the resource.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class FeatureAddedEvent<F extends MBeanFeatureInfo> extends ResourceEvent implements FeatureRead<F> {
    private static final long serialVersionUID = 3183942685265765987L;
    private final F feature;

    public FeatureAddedEvent(final Object sender, final F addedFeature){
        super(sender);
        this.feature = Objects.requireNonNull(addedFeature);
    }

    /**
     * Gets a feature added to the managed resource connector.
     * @return The feature added to the managed resource connector.
     */
    public final F getFeature(){
        return feature;
    }
}
