package com.bytex.snamp.supervision.discovery;

import javax.annotation.Nonnull;
import java.util.EventListener;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ResourceDiscoveryEventListener extends EventListener {
    void resourceChanged(@Nonnull final ResourceDiscoveryEvent event);
}
