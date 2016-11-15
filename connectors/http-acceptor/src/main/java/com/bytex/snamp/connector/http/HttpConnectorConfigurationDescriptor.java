package com.bytex.snamp.connector.http;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.connector.md.MessageDrivenConnectorConfigurationDescriptor;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HttpConnectorConfigurationDescriptor extends MessageDrivenConnectorConfigurationDescriptor {
    private static final LazySoftReference<HttpConnectorConfigurationDescriptor> INSTANCE = new LazySoftReference<>();

    private HttpConnectorConfigurationDescriptor(){

    }



    static HttpConnectorConfigurationDescriptor getInstance(){
        return INSTANCE.lazyGet(HttpConnectorConfigurationDescriptor::new);
    }
}
