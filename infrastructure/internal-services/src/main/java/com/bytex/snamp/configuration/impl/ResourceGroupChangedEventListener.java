package com.bytex.snamp.configuration.impl;

import java.util.EventListener;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface ResourceGroupChangedEventListener extends EventListener {
    void groupNameChanged(final ResourceGroupChangedEvent event);
}
