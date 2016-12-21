package com.bytex.snamp.cluster;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.server.distributed.ORemoteServerController;
import com.orientechnologies.orient.server.hazelcast.OHazelcastPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
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

    private void shutdownDistributedPlugin() {
        // CLOSE ALL CONNECTIONS TO THE SERVERS
        remoteServers.values().forEach(ORemoteServerController::close);
        remoteServers.clear();

        if (publishLocalNodeConfigurationTask != null)
            publishLocalNodeConfigurationTask.cancel();

        if (healthCheckerTask != null)
            healthCheckerTask.cancel();

        if (messageService != null)
            messageService.shutdown();

        activeNodes.clear();
        activeNodesNamesByUuid.clear();
        activeNodesUuidByName.clear();

        setNodeStatus(NODE_STATUS.OFFLINE);

        Orient.instance().removeDbLifecycleListener(this);

        // CLOSE AND FREE ALL THE STORAGES
        storages.values().forEach(s -> {
            try {
                s.shutdownAsynchronousWorker();
                s.close();
            } catch (final Exception e) {
                OLogManager.instance().error(this, "Failed to close storage", e);
            }
        });
        storages.clear();
    }

    @Override
    public void shutdown() {
        if (!enabled)
            return;

        OLogManager.instance().warn(this, "Shutting down node '%s'...", nodeName);
        setNodeStatus(NODE_STATUS.SHUTTINGDOWN);

        try {
            final Set<String> databases = new HashSet<>();

            configurationMap.entrySet().stream().filter(entry -> entry.getKey().startsWith(CONFIG_DBSTATUS_PREFIX)).forEach(entry -> {

                final String nodeDb = entry.getKey().substring(CONFIG_DBSTATUS_PREFIX.length());

                if (nodeDb.startsWith(nodeName))
                    databases.add(entry.getKey());
            });

            // PUT DATABASES AS NOT_AVAILABLE
            for (final String k : databases)
                configurationMap.put(k, DB_STATUS.NOT_AVAILABLE);

        } catch (final HazelcastInstanceNotActiveException e) {
            OLogManager.instance().error(this, "Hazelcast is already down", e);
        }

        shutdownDistributedPlugin();

        if (membershipListenerRegistration != null) {
            try {
                hazelcastInstance.getCluster().removeMembershipListener(membershipListenerRegistration);
            } catch (HazelcastInstanceNotActiveException e) {
                OLogManager.instance().error(this, "Hazelcast is already down", e);
            }
        }

        hazelcastInstance = null;     //do not shutdown Hazelcast

        try {
            configurationMap.destroy();
        } catch (final Exception e) {
            OLogManager.instance().warn(this, "Failed to destroy distributed configuration map", e);
        }

        try {
            configurationMap.getHazelcastMap().removeEntryListener(membershipListenerMapRegistration);
        } catch (final Exception e) {
            OLogManager.instance().warn(this, "Failed to remove entry listener", e);
        } finally {
            configurationMap = null;
        }
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
