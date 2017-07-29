package com.bytex.snamp.supervision;

import javax.annotation.Nonnull;
import java.util.EventListener;

/**
 * Represents listener of supervision events.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface SupervisionEventListener extends EventListener {
    void handle(@Nonnull final SupervisionEvent event, final Object handback);
}
