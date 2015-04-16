package com.itworks.snamp.adapters.xmpp;

import com.itworks.snamp.adapters.NotificationEvent;

import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AllowedNotifications extends ConcurrentSkipListSet<String> {
    private static final long serialVersionUID = 5850511800004896950L;
    private boolean allowAll = false;

    /**
     * Allows all notifications.
     */
    public void allowAll(){
        allowAll = true;
        super.clear();
    }

    public boolean isAny(){
        return allowAll;
    }

    /**
     * Enables notifications of the specified managed resource.
     * @param resourceName The name of the managed resource.
     */
    @Override
    public boolean add(final String resourceName) {
        allowAll = false;
        return super.add(resourceName);
    }

    /**
     * Disables all notifications
     */
    @Override
    public void clear() {
        allowAll = false;
        super.clear();
    }

    public boolean isAllowed(final NotificationEvent event){
        return contains(event.getNotification().getSource());
    }

    /**
     * Determines whether the specified resource is allowed.
     * @param resourceName The name of the managed resource.
     * @return {@literal true}, if notifications of the specified resource are allowed; otherwise, {@literal false}.
     */
    @Override
    public boolean contains(final Object resourceName) {
        return allowAll || super.contains(resourceName);
    }
}
