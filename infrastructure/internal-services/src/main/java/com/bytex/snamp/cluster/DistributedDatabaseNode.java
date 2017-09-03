package com.bytex.snamp.cluster;

import com.hazelcast.core.HazelcastInstance;

import javax.management.JMException;
import java.io.IOException;

/**
 * Represents database node in distributed environment.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
final class DistributedDatabaseNode extends DatabaseNode {
    DistributedDatabaseNode(final HazelcastInstance hazelcast) throws ClassNotFoundException, JMException, IOException {
        distributedManager = new OrientDistributedEnvironment(hazelcast);
    }
}
