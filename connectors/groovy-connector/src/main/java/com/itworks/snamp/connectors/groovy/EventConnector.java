package com.itworks.snamp.connectors.groovy;

import com.itworks.snamp.connectors.notifications.NotificationDescriptor;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface EventConnector {
    NotificationEmitter loadEvent(final String scriptFile,
                                  final NotificationEmitter realEmitter) throws ResourceException, ScriptException;

    NotificationEmitter loadEvent(final NotificationDescriptor descriptor,
                                  final NotificationEmitter realEmitter) throws ResourceException, ScriptException;
}
