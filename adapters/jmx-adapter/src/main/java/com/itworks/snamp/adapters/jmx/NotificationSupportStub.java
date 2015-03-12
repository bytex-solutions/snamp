package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.internal.annotations.MethodStub;
import com.itworks.snamp.jmx.JMExceptionUtils;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NotificationSupportStub implements NotificationSupport {
    static final NotificationSupportStub INSTANCE = new NotificationSupportStub();

    private final MBeanNotificationInfo[] notifications;

    private NotificationSupportStub(){
        notifications = new MBeanNotificationInfo[0];
    }

    @Override
    @MethodStub
    public void addNotificationListener(final NotificationListener listener, final NotificationFilter filter, final Object handback) {

    }

    @Override
    public void removeNotificationListener(final NotificationListener listener) throws ListenerNotFoundException {
        throw JMExceptionUtils.listenerNotFound(listener);
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return notifications;
    }
}
