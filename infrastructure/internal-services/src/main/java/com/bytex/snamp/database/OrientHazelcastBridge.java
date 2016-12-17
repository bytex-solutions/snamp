package com.bytex.snamp.database;

import com.hazelcast.core.HazelcastInstance;
import com.orientechnologies.orient.server.hazelcast.OHazelcastPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

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
    protected HazelcastInstance configureHazelcast() throws FileNotFoundException {
        return hazelcastInstance;
    }

    @Override
    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }
}
