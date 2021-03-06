package com.bytex.snamp.connector;

import javax.annotation.Nonnull;
import javax.management.MBeanFeatureInfo;

/**
 * Indicates that the feature provided by managed resource was modified.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class FeatureModifiedEvent<F extends MBeanFeatureInfo> extends ResourceEvent {
    /**
     * Represents modification type.
     * @since 2.0
     */
    public enum Modifier {
        /**
         * Indicates that feature was added.
         */
        ADDED,
        /**
         * Indicates that feature is removing.
         */
        REMOVING
    }
    private static final long serialVersionUID = -4019967787362487363L;
    private final F feature;
    private final Modifier modification;

    protected FeatureModifiedEvent(@Nonnull final Object sender,
                         final String resourceName,
                         @Nonnull final F feature,
                         @Nonnull final Modifier type) {
        super(sender, resourceName);
        this.feature = feature;
        modification = type;
    }

    /**
     * Gets modification type.
     * @return Modification type.
     */
    public final Modifier getModifier(){
        return modification;
    }

    public final F getFeature(){
        return feature;
    }
}
