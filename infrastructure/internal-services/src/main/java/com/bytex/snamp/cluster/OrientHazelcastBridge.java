package com.bytex.snamp.cluster;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.common.util.OCallableUtils;
import com.orientechnologies.orient.server.hazelcast.OHazelcastPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class OrientHazelcastBridge extends OHazelcastPlugin {

    OrientHazelcastBridge(final HazelcastInstance instance) throws IOException {
        hazelcastInstance = Objects.requireNonNull(instance);
        hazelcastConfig = instance.getConfig();
        String machineName;
        try {
            machineName = InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            machineName = ManagementFactory.getRuntimeMXBean().getName();
        }
        nodeName = "OrientDB-" + machineName + '-' + System.currentTimeMillis();
        //read OrientDB distributed configuration
        defaultDatabaseConfigFile = DatabaseConfigurationFile.DISTRIBUTED_CONFIG.toFile(true);
    }

    @Override
    public void shutdown() {
        if (!enabled)
            return;

        OLogManager.instance().warn(this, "Shutting down node '%s'...", nodeName);
        setNodeStatus(NODE_STATUS.SHUTTINGDOWN);

        try {
            final Set<String> databases = new HashSet<String>();

            for (Map.Entry<String, Object> entry : configurationMap.entrySet()) {
                if (entry.getKey().startsWith(CONFIG_DBSTATUS_PREFIX)) {

                    final String nodeDb = entry.getKey().substring(CONFIG_DBSTATUS_PREFIX.length());

                    if (nodeDb.startsWith(nodeName))
                        databases.add(entry.getKey());
                }
            }

            // PUT DATABASES AS NOT_AVAILABLE
            for (String k : databases)
                configurationMap.put(k, DB_STATUS.NOT_AVAILABLE);

        } catch (HazelcastInstanceNotActiveException e) {
            // HZ IS ALREADY DOWN, IGNORE IT
        }

        super.shutdown();

        if (membershipListenerRegistration != null) {
            try {
                hazelcastInstance.getCluster().removeMembershipListener(membershipListenerRegistration);
            } catch (HazelcastInstanceNotActiveException e) {
                // HZ IS ALREADY DOWN, IGNORE IT
            }
        }

        hazelcastInstance = null;     //do not shutdown Hazelcast

        OCallableUtils.executeIgnoringAnyExceptions(() -> configurationMap.destroy());

        OCallableUtils.executeIgnoringAnyExceptions(() -> configurationMap.getHazelcastMap().removeEntryListener(membershipListenerMapRegistration));

        setNodeStatus(NODE_STATUS.OFFLINE);
    }

    @Override
    protected HazelcastInstance configureHazelcast() throws FileNotFoundException {
        return hazelcastInstance;
    }

    @Override
    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }
}
