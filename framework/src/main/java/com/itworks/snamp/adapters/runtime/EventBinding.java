package com.itworks.snamp.adapters.runtime;

import com.itworks.snamp.adapters.FeatureAccessor;

/**
 * Represents information about binding of the attribute.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class EventBinding extends FeatureBinding {
    EventBinding(final String declaredResource, final FeatureAccessor<?, ?> accessor) {
        super(declaredResource, accessor);
    }

    EventBinding(final String declaredResource,
                 final String listID) {
        super(declaredResource, listID);
    }

    /**
     * Gets information about attachment type of this event binding.
     * @return The information about attachment type of this event binding.
     */
    public abstract Object getAttachmentType();
}
