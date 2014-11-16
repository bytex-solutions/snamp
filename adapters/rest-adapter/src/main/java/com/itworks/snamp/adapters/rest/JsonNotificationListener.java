package com.itworks.snamp.adapters.rest;

import com.google.common.eventbus.Subscribe;

import java.util.EventListener;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface JsonNotificationListener extends EventListener {
    @Subscribe
    void processNotification(final JsonNotification notification);
}
