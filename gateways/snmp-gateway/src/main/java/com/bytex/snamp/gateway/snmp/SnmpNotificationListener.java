package com.bytex.snamp.gateway.snmp;

import java.util.EventListener;

/**
 * Represents SNMP notification listener.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
interface SnmpNotificationListener extends EventListener {
    void processNotification(final SnmpNotification wrappedNotification);
}
