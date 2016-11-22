package com.bytex.snamp.connector.zipkin;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.connector.md.MessageDrivenConnectorConfigurationDescriptor;
import com.google.common.base.Splitter;
import zipkin.collector.CollectorComponent;
import zipkin.collector.kafka.KafkaCollector;
import zipkin.storage.StorageComponent;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ZipkinConnectorConfigurationDescriptionProvider extends MessageDrivenConnectorConfigurationDescriptor {
    private static final Splitter PATH_SPLITTER = Splitter.on('/').omitEmptyStrings();
    private static final LazySoftReference<ZipkinConnectorConfigurationDescriptionProvider> INSTANCE = new LazySoftReference<>();

    private ZipkinConnectorConfigurationDescriptionProvider(){
        super();
    }

    static ZipkinConnectorConfigurationDescriptionProvider getInstance(){
        return INSTANCE.lazyGet(ZipkinConnectorConfigurationDescriptionProvider::new);
    }

    private static CollectorComponent createKafkaCollector(final URI connectionString, final StorageComponent storage){
        final String zookeeperAddress = connectionString.getHost() + ':' + (connectionString.getPort() < 0 ? 2181 : connectionString.getPort());
        final List<String> parts = PATH_SPLITTER.splitToList(connectionString.getPath());
        final String topic;
        final String groupID;
        switch (parts.size()){
            case 1: //only topic is specified
                topic = parts.get(0);
                groupID = "zipkin";
                break;
            case 2: //groupID/topic
                groupID = parts.get(0);
                topic = parts.get(1);
                break;
            default:
                topic = groupID = "zipkin";
        }
        return KafkaCollector.builder()
                .zookeeper(zookeeperAddress)
                .streams(1)
                .storage(storage)
                .groupId(groupID)
                .topic(topic)
                .build();
    }

    CollectorComponent createCollector(final String connectionString, final StorageComponent storage) throws URISyntaxException {
        if(isNullOrEmpty(connectionString))
            return null;
        final URI url = new URI(connectionString);
        switch (url.getScheme()){
            case "kafka":
                return createKafkaCollector(url, storage);
            default:
                throw new URISyntaxException(connectionString, String.format("Unsupported protocol %s", url.getScheme()));
        }
    }
}
