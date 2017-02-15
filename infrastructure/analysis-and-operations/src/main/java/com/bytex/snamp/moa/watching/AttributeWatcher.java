package com.bytex.snamp.moa.watching;

import java.util.EventListener;

/**
 * Handles a new attribute state.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
public interface AttributeWatcher extends EventListener {
    void attributeStateChanged(final AttributeWatchEvent event);
}
