package com.bytex.snamp.connector.zipkin;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.connector.md.MessageDrivenConnectorConfigurationDescriptor;
import zipkin.collector.CollectorComponent;
import zipkin.storage.StorageComponent;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ZipkinConnectorConfigurationDescriptionProvider extends MessageDrivenConnectorConfigurationDescriptor {
    private static final LazySoftReference<ZipkinConnectorConfigurationDescriptionProvider> INSTANCE = new LazySoftReference<>();

    private ZipkinConnectorConfigurationDescriptionProvider(){

    }

    static ZipkinConnectorConfigurationDescriptionProvider getInstance(){
        return INSTANCE.lazyGet(ZipkinConnectorConfigurationDescriptionProvider::new);
    }

    CollectorComponent createCollector(final StorageComponent storage){
        return null;
    }
}
