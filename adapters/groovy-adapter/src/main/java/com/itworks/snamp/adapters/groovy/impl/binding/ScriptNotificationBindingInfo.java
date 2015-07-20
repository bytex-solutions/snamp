package com.itworks.snamp.adapters.groovy.impl.binding;

import com.itworks.snamp.adapters.groovy.impl.ScriptNotificationAccessor;
import com.itworks.snamp.adapters.binding.NotificationBindingInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ScriptNotificationBindingInfo extends NotificationBindingInfo {

    ScriptNotificationBindingInfo(final ScriptNotificationAccessor accessor) {
        super(accessor.getResourceName(), accessor);
    }

    /**
     * Gets information about attachment type of this event binding.
     *
     * @return The information about attachment type of this event binding.
     */
    @Override
    public Object getAttachmentType() {
        return null;
    }
}
