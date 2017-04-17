package com.bytex.snamp.supervision.discovery;

import com.bytex.snamp.supervision.SupervisorService;

import javax.annotation.Nonnull;

/**
 * Represents service for resource registration.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ResourceDiscoveryService extends SupervisorService {
    void addDiscoveryEventListener(@Nonnull final ResourceDiscoveryEventListener listener);
    void removeDiscoveryEventListener(@Nonnull final ResourceDiscoveryEventListener listener);
}
