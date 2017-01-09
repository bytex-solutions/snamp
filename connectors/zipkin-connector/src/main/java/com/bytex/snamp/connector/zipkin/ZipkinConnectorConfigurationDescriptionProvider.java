package com.bytex.snamp.connector.zipkin;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.connector.dsp.DataStreamDrivenConnectorConfigurationDescriptionProvider;
import com.google.common.base.Splitter;
import zipkin.collector.CollectorComponent;
import zipkin.collector.kafka.KafkaCollector;
import zipkin.storage.StorageComponent;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.internal.Utils.callUnchecked;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ZipkinConnectorConfigurationDescriptionProvider extends DataStreamDrivenConnectorConfigurationDescriptionProvider {
    private static final Splitter URL_PATH_SPLITTER = Splitter.on('/').omitEmptyStrings();
    private static final Splitter SCRIPT_PATH_SPLITTER = Splitter.on(';').omitEmptyStrings().trimResults();
    private static final String PARSER_SCRIPT_PATH_PARAM = "parserScriptPath";
    private static final String PARSER_SCRIPT_NAME_PARAM = "parserScript";


    private static final LazySoftReference<ZipkinConnectorConfigurationDescriptionProvider> INSTANCE = new LazySoftReference<>();

    private static final class ZipkinConnectorConfigurationDescription extends ConnectorConfigurationDescription{
        private ZipkinConnectorConfigurationDescription(){
            super("ConnectorParameters", PARSER_SCRIPT_NAME_PARAM, PARSER_SCRIPT_PATH_PARAM);
        }
    }

    private ZipkinConnectorConfigurationDescriptionProvider(){
        super(new ZipkinConnectorConfigurationDescription(), AttributeConfigurationDescription.createDefault(), EventConfigurationDescription.createDefault());
    }

    static ZipkinConnectorConfigurationDescriptionProvider getInstance(){
        return INSTANCE.lazyGet(ZipkinConnectorConfigurationDescriptionProvider::new);
    }

    private static CollectorComponent createKafkaCollector(final URI connectionString, final StorageComponent storage){
        final String zookeeperAddress = connectionString.getHost() + ':' + (connectionString.getPort() < 0 ? 2181 : connectionString.getPort());
        final List<String> parts = URL_PATH_SPLITTER.splitToList(connectionString.getPath());
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

    URL[] parseScriptPath(final Map<String, String> parameters){
        final String path = getValue(parameters, PARSER_SCRIPT_PATH_PARAM, Function.identity(), EMPTY_STRING);
        return SCRIPT_PATH_SPLITTER.splitToList(path).stream().map(p -> callUnchecked((() -> new URL(p)))).toArray(URL[]::new);
    }

    String parseScriptFile(final Map<String, String> parameters){
        return getValue(parameters, PARSER_SCRIPT_NAME_PARAM, Function.identity(), () -> "ZipkinSpanParser.groovy");
    }
}
