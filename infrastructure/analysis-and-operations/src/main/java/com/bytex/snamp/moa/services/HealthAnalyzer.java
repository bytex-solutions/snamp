package com.bytex.snamp.moa.services;

import com.bytex.snamp.configuration.ManagedResourceGroupWatcherConfiguration;
import com.bytex.snamp.connector.supervision.HealthSupervisor;
import org.osgi.service.cm.ManagedService;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface HealthAnalyzer extends HealthSupervisor, ManagedService {
    String getPersistentID();

    @Nonnull
    @Override
    Map<String, UpdatableGroupWatcher> getConfiguration();
}
