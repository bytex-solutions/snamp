package com.bytex.snamp.adapters.modeling;

import com.bytex.snamp.internal.AbstractKeyedObjects;

import javax.management.MBeanFeatureInfo;

/**
 * Represents a collection of managed resource features.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public abstract class ResourceFeatureList<M extends MBeanFeatureInfo, TAccessor extends FeatureAccessor<M>> extends AbstractKeyedObjects<String, TAccessor> {
    private static final long serialVersionUID = 7793182286593325522L;

    /**
     * Initializes a new list of resource features.
     *
     * @param capacity The initial capacity of this object.
     */
    protected ResourceFeatureList(final int capacity) {
        super(capacity);
    }

    /**
     * Gets identity of the managed resource feature.
     * @param feature The managed resource feature.
     * @return The identity of the managed resource feature.
     * @see javax.management.MBeanAttributeInfo#getName()
     * @see javax.management.MBeanNotificationInfo#getNotifTypes()
     */
    protected abstract String getKey(final M feature);

    /**
     * Gets feature identity.
     * @param accessor Managed resource feature accessor. Cannot be {@literal null}.
     * @return The identity of the managed resource feature.
     */
    @Override
    public final String getKey(final TAccessor accessor) {
        return getKey(accessor.getMetadata());
    }

    public final boolean containsKey(final M feature){
        return containsKey(getKey(feature));
    }

    public final TAccessor get(final M metadata){
        return get(getKey(metadata));
    }

    public final TAccessor remove(final M metadata){
        return remove(getKey(metadata));
    }

    @Override
    public final void clear() {
        values().forEach(FeatureAccessor::close);
        super.clear();
    }
}
