package com.itworks.snamp.connectors;

import javax.management.MBeanFeatureInfo;
import java.util.Objects;

/**
 * Represents an event raised when managed resource connector removes
 * the feature.
 * @param <F> Type of the feature to remove.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class FeatureRemovedEvent<F extends MBeanFeatureInfo> extends ResourceEvent implements FeatureRead<F> {
    private static final long serialVersionUID = 1715564298586657421L;
    private final F feature;

    public FeatureRemovedEvent(final Object sender, final F removedFeature) {
        super(sender);
        this.feature = Objects.requireNonNull(removedFeature);
    }

    /**
     * Gets feature removed from the managed resource.
     *
     * @return The feature of the managed resource.
     */
    @Override
    public F getFeature() {
        return feature;
    }
}
