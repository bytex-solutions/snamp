package com.bytex.snamp.moa.services;

import com.bytex.snamp.gateway.Gateway;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import com.bytex.snamp.moa.watching.WatcherService;

/**
 * Represents uber interface of all services with analytics.
 */
interface AnalyticalCenter extends Gateway {
    TopologyAnalyzer getTopologyAnalyzer();
    WatcherService getWatcherService();
}
