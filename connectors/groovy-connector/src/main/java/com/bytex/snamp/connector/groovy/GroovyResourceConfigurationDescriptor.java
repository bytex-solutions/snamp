package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.bytex.snamp.connector.ManagedResourceDescriptionProvider;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class GroovyResourceConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl implements ManagedResourceDescriptionProvider {

    private static final class ConnectorConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        private static final String RESOURCE_NAME = "ConnectorParameters";

        private ConnectorConfigurationInfo(){
            super(RESOURCE_NAME,
                    ManagedResourceConfiguration.class,
                    "groovy.warnings",
                    "groovy.source.encoding",
                    "groovy.classpath",
                    "groovy.output.verbose",
                    "groovy.output.debug",
                    "groovy.errors.tolerance");
        }
    }

    private static final LazySoftReference<GroovyResourceConfigurationDescriptor> INSTANCE = new LazySoftReference<>();

    private GroovyResourceConfigurationDescriptor(){
        super(new ConnectorConfigurationInfo());
    }

    static GroovyResourceConfigurationDescriptor getInstance(){
        return INSTANCE.lazyGet(GroovyResourceConfigurationDescriptor::new);
    }
}
