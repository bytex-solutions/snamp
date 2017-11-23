package com.bytex.snamp.connector;

import javax.annotation.Nonnull;
import javax.management.MBeanFeatureInfo;

/**
 * Indicates that the feature provided by managed resource was modified.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public class FeatureModifiedEvent extends ResourceEvent {
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
    private final MBeanFeatureInfo feature;
    private final Modifier modification;

    public FeatureModifiedEvent(@Nonnull final ManagedResourceConnector sender,
                         final String resourceName,
                         @Nonnull final MBeanFeatureInfo feature,
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

    public final MBeanFeatureInfo getFeature(){
        return feature;
    }
}
