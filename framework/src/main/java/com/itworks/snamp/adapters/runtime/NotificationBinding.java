package com.itworks.snamp.adapters.runtime;

import com.itworks.snamp.adapters.FeatureAccessor;
import com.itworks.snamp.adapters.NotificationAccessor;

/**
 * Represents information about binding of the attribute.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class NotificationBinding extends FeatureBinding {
    protected NotificationBinding(final String declaredResource, final NotificationAccessor accessor) {
        super(declaredResource, accessor);
    }

    protected NotificationBinding(final String declaredResource,
                                  final String listID) {
        super(declaredResource, listID);
    }

    /**
     * Gets information about attachment type of this event binding.
     * @return The information about attachment type of this event binding.
     */
    public abstract Object getAttachmentType();
}
