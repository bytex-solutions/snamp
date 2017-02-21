package com.bytex.snamp.supervision;

import java.util.EventListener;

/**
 *
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
public interface GroupStatusEventListener extends EventListener {
    void statusChanged(final GroupStatusChangedEvent event);
}
