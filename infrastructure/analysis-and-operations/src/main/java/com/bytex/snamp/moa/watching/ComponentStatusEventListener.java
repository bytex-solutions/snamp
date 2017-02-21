package com.bytex.snamp.moa.watching;

import java.util.EventListener;

/**
 *
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
public interface ComponentStatusEventListener extends EventListener {
    void statusChanged(final ComponentStatusChangedEvent event);
}
