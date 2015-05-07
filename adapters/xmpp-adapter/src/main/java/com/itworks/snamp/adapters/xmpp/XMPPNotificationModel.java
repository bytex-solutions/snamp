package com.itworks.snamp.adapters.xmpp;

import com.google.common.collect.ImmutableList;
import com.itworks.snamp.adapters.AbstractNotificationBus;
import com.itworks.snamp.adapters.ResourceNotificationList;

import javax.management.MBeanNotificationInfo;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class XMPPNotificationModel extends AbstractNotificationBus {
    private enum XNMResource{
        LISTENERS,
        NOTIFICATIONS
    }

    private final Map<String, ResourceNotificationList<XMPPNotificationAccessor>> notifications;

    XMPPNotificationModel(){
        super(XNMResource.class, XNMResource.LISTENERS);
        notifications = new HashMap<>(10);
    }

    XMPPNotificationAccessor enableNotifications(final String resourceName,
                             final MBeanNotificationInfo metadata){
        try(final LockScope ignored = beginWrite(XNMResource.NOTIFICATIONS)){
            final ResourceNotificationList<XMPPNotificationAccessor> resource;
            if(notifications.containsKey(resourceName))
                resource = notifications.get(resourceName);
            else notifications.put(resourceName, resource = new ResourceNotificationList<>());
            final XMPPNotificationAccessor router = new XMPPNotificationAccessor(metadata,
                    this,
                    resourceName);
            resource.put(router);
            return router;
        }
    }

    XMPPNotificationAccessor disableNotifications(final String resourceName,
                                              final MBeanNotificationInfo metadata){
        try(final LockScope ignored = beginWrite(XNMResource.NOTIFICATIONS)){
            final ResourceNotificationList<XMPPNotificationAccessor> resource =
                    notifications.get(resourceName);
            if(resource == null) return null;
            final XMPPNotificationAccessor accessor = resource.remove(metadata);
            if(resource.isEmpty())
                notifications.remove(resourceName);
            return accessor;
        }
    }

    Iterable<XMPPNotificationAccessor> clear(final String resourceName){
        try(final LockScope ignored = beginWrite(XNMResource.NOTIFICATIONS)){
            final ResourceNotificationList<XMPPNotificationAccessor> resource =
                    notifications.remove(resourceName);
            return resource != null ? resource.values() : ImmutableList.<XMPPNotificationAccessor>of();
        }
    }

    void clear(){
        removeAllListeners();
        try(final LockScope ignored = beginWrite(XNMResource.NOTIFICATIONS)){
            for(final ResourceNotificationList<?> list: notifications.values())
                list.clear();
            notifications.clear();
        }
    }
}
