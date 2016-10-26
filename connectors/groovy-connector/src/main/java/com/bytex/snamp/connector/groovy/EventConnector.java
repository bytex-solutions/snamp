package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface EventConnector {
    NotificationEmitter loadEvent(final String scriptFile,
                                  final NotificationEmitter realEmitter) throws ResourceException, ScriptException;

    NotificationEmitter loadEvent(final String notifType,
                                  final NotificationDescriptor descriptor,
                                  final NotificationEmitter realEmitter) throws ResourceException, ScriptException;
}
