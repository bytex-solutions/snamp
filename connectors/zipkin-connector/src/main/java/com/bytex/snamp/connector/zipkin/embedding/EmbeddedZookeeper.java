package com.bytex.snamp.connector.zipkin.embedding;

import com.bytex.snamp.internal.Utils;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;

import java.io.IOException;
import java.util.Properties;

//https://gist.github.com/fjavieralba/7930018
final class EmbeddedZookeeper extends Thread {
    private final ServerConfig configuration;

    EmbeddedZookeeper(final Properties zkProperties) throws IOException, QuorumPeerConfig.ConfigException {
        final QuorumPeerConfig quorumConfiguration = new QuorumPeerConfig();
        quorumConfiguration.parseProperties(zkProperties);
        configuration = new ServerConfig();
        configuration.readFrom(quorumConfiguration);
    }

    @Override
    public void run() {
        Utils.callUnchecked(() -> {
            final ZooKeeperServerMain zooKeeperServer = new ZooKeeperServerMain();
            zooKeeperServer.runFromConfig(configuration);
            return null;
        });
    }
}
