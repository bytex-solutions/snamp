package com.bytex.snamp.moa.services;

import com.bytex.snamp.connector.supervision.HealthStatusProvider;
import org.osgi.service.cm.ManagedService;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface HealthAnalyzer extends HealthStatusProvider, ManagedService {
    String getPersistentID();

    @Nonnull
    @Override
    Map<String, UpdatableGroupWatcher> getConfiguration();
}
