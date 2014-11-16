package com.itworks.snamp.adapters.snmp;

import com.google.common.eventbus.Subscribe;

import java.util.EventListener;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface SnmpNoitificationListener extends EventListener {
    @Subscribe
    void processNotification(final SnmpNotification wrappedNotification);
}
