package com.bytex.snamp.connector.zipkin.embedding;

import com.bytex.snamp.SafeCloseable;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;

import java.io.IOException;
import java.util.Properties;

public final class EmbeddedKafka implements AutoCloseable {
    private final KafkaServerStartable kafka;
    private final EmbeddedZookeeper zookeeper;

    public EmbeddedKafka(final Properties kafkaProperties, Properties zkProperties) throws IOException, QuorumPeerConfig.ConfigException {
        final KafkaConfig kafkaConfig = new KafkaConfig(kafkaProperties);
        zookeeper = new EmbeddedZookeeper(zkProperties);
        kafka = new KafkaServerStartable(kafkaConfig);
    }

    public void start(){
        //start local zookeeper
        System.out.println("starting local zookeeper...");
        zookeeper.start();
        System.out.println("done");

        //start local kafka broker
        System.out.println("starting local kafka broker...");
        kafka.startup();
        System.out.println("done");
    }

    @Override
    public void close() throws InterruptedException {
        //stop kafka broker
        System.out.println("stopping kafka...");
        kafka.shutdown();
        System.out.println("done");

        System.out.println("stopping zookeeper...");
        zookeeper.interrupt();
        zookeeper.join();
        System.out.println("done");
    }

}
