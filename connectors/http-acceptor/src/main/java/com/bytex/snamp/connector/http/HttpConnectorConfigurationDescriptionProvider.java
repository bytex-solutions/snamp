package com.bytex.snamp.connector.http;

import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.connector.dataStream.DataStreamConnectorConfigurationDescriptionProvider;
import com.google.common.base.Splitter;

import java.net.URL;
import java.util.Map;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class HttpConnectorConfigurationDescriptionProvider extends DataStreamConnectorConfigurationDescriptionProvider {
    private static final Splitter PATH_SPLITTER = Splitter.on(';').omitEmptyStrings().trimResults();
    private static final String PARSER_SCRIPT_PATH_PARAM = "parserScriptPath";
    private static final String PARSER_SCRIPT_NAME_PARAM = "parserScript";

    private static final LazyReference<HttpConnectorConfigurationDescriptionProvider> INSTANCE = LazyReference.soft();

    private static final class HttpConnectorConfigurationDescription extends ConnectorConfigurationDescription{
        private HttpConnectorConfigurationDescription(){
            super("ConnectorParameters", PARSER_SCRIPT_NAME_PARAM, PARSER_SCRIPT_PATH_PARAM);
        }
    }

    private HttpConnectorConfigurationDescriptionProvider(){
        super(new HttpConnectorConfigurationDescription(), AttributeConfigurationDescription.createDefault(), EventConfigurationDescription.createDefault());
    }

    static HttpConnectorConfigurationDescriptionProvider getInstance(){
        return INSTANCE.get(HttpConnectorConfigurationDescriptionProvider::new);
    }

    URL[] parseScriptPath(final Map<String, String> parameters){
        final String path = getValue(parameters, PARSER_SCRIPT_PATH_PARAM, Function.identity()).orElse("");
        return PATH_SPLITTER.splitToList(path).stream().map(p -> callUnchecked((() -> new URL(p)))).toArray(URL[]::new);
    }

    String parseScriptFile(final Map<String, String> parameters){
        return getValue(parameters, PARSER_SCRIPT_NAME_PARAM, Function.identity()).orElse("");
    }
}
