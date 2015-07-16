package com.itworks.snamp.adapters.runtime;

import com.itworks.snamp.adapters.AttributeAccessor;

/**
 * Represents information about binding of the attribute.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AttributeBinding extends FeatureBinding {
    protected AttributeBinding(final String declaredResource,
                               final AttributeAccessor accessor) {
        super(declaredResource, accessor);
    }

    protected AttributeBinding(final String declaredResource, final String attributeID) {
        super(declaredResource, attributeID);
    }

    /**
     * Gets information about attribute type inside of the adapter.
     * @return The information about attribute type inside of the adapter.
     */
    public abstract Object getMappedType();
}
