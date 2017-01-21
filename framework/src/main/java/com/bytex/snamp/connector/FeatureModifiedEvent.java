package com.bytex.snamp.connector;

import javax.management.MBeanFeatureInfo;
import java.util.Objects;

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
    public enum ModificationType{
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
    private final ModificationType modification;

    protected FeatureModifiedEvent(final Object sender,
                         final String resourceName,
                         final F feature,
                         final ModificationType type) {
        super(sender, resourceName);
        this.feature = Objects.requireNonNull(feature);
        modification = Objects.requireNonNull(type);
    }

    /**
     * Gets modification type.
     * @return Modification type.
     */
    public final ModificationType getType(){
        return modification;
    }

    public final F getFeature(){
        return feature;
    }
}
