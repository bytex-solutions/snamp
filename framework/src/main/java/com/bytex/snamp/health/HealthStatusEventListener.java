package com.bytex.snamp.health;

import java.util.EventListener;

/**
 *
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
public interface HealthStatusEventListener extends EventListener {
    void statusChanged(final HealthStatusChangedEvent event);
}
