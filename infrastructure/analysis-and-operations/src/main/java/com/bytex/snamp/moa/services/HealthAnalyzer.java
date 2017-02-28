package com.bytex.snamp.moa.services;

import com.bytex.snamp.connector.supervision.HealthSupervisor;
import org.osgi.service.cm.ManagedService;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface HealthAnalyzer extends HealthSupervisor, ManagedService {
    String getPersistentID();
}
