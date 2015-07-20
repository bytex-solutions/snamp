package com.itworks.snamp.adapters.http.binding;

import com.itworks.snamp.adapters.http.HttpNotificationAccessor;
import com.itworks.snamp.adapters.binding.NotificationBindingInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class HttpNotificationBindingInfo extends NotificationBindingInfo {
    HttpNotificationBindingInfo(final String servletContext,
                                final String resourceName,
                                final HttpNotificationAccessor accessor){
        super(resourceName, accessor);
        put("path", accessor.getPath(servletContext, resourceName));
    }

    @Override
    public Object getAttachmentType() {
        return null;
    }
}
