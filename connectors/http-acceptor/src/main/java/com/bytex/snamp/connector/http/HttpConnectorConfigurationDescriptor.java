package com.bytex.snamp.connector.http;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.connector.md.MessageDrivenConnectorConfigurationDescriptor;
import com.google.common.base.Splitter;
import static com.bytex.snamp.MapUtils.*;
import static com.bytex.snamp.internal.Utils.callUnchecked;

import java.net.URL;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HttpConnectorConfigurationDescriptor extends MessageDrivenConnectorConfigurationDescriptor {
    private static final Supplier<String> EMPTY_STRING = () -> "";
    private static final Splitter PATH_SPLITTER = Splitter.on(';').omitEmptyStrings().trimResults();
    private static final String PARSER_SCRIPT_PATH_PARAM = "parserScriptPath";
    private static final String PARSER_SCRIPT_NAME_PARAM = "parserScript";

    private static final LazySoftReference<HttpConnectorConfigurationDescriptor> INSTANCE = new LazySoftReference<>();

    private HttpConnectorConfigurationDescriptor(){

    }

    static HttpConnectorConfigurationDescriptor getInstance(){
        return INSTANCE.lazyGet(HttpConnectorConfigurationDescriptor::new);
    }

    URL[] parseScriptPath(final Map<String, String> parameters){
        final String path = getValue(parameters, PARSER_SCRIPT_PATH_PARAM, Function.identity(), EMPTY_STRING);
        return PATH_SPLITTER.splitToList(path).stream().map(p -> callUnchecked((() -> new URL(p)))).toArray(URL[]::new);
    }

    String parseScriptFile(final Map<String, String> parameters){
        return getValue(parameters, PARSER_SCRIPT_NAME_PARAM, Function.identity(), EMPTY_STRING);
    }
}
