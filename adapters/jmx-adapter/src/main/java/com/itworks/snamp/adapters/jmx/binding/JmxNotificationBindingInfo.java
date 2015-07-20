package com.itworks.snamp.adapters.jmx.binding;

import com.itworks.snamp.adapters.binding.NotificationBindingInfo;
import com.itworks.snamp.adapters.jmx.JmxNotificationAccessor;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxNotificationBindingInfo extends NotificationBindingInfo {
    JmxNotificationBindingInfo(final String declaredResource,
                               final JmxNotificationAccessor accessor){
        super(declaredResource, accessor);
    }

    @Override
    public Object getAttachmentType() {
        return null;
    }
}
