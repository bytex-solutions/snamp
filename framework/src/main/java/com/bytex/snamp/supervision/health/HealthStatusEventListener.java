package com.bytex.snamp.supervision.health;

import javax.annotation.Nonnull;
import java.util.EventListener;

/**
 *
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
public interface HealthStatusEventListener extends EventListener {
    void statusChanged(@Nonnull final HealthStatusChangedEvent event);
}
