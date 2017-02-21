package com.bytex.snamp.health;

import com.bytex.snamp.core.FrameworkService;
import com.bytex.snamp.moa.DataAnalyzer;

import java.util.Set;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface HealthCheckService extends FrameworkService, DataAnalyzer {
    Set<String> getWatchingComponents();
}
