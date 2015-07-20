package com.itworks.snamp.adapters.binding;

import com.itworks.snamp.adapters.NotificationAccessor;

/**
 * Represents information about binding of the attribute.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class NotificationBindingInfo extends FeatureBindingInfo {
    protected NotificationBindingInfo(final String declaredResource, final NotificationAccessor accessor) {
        super(declaredResource, accessor);
    }

    protected NotificationBindingInfo(final String declaredResource,
                                      final String listID) {
        super(declaredResource, listID);
    }

    /**
     * Gets information about attachment type of this event binding.
     * @return The information about attachment type of this event binding.
     */
    public abstract Object getAttachmentType();
}
