package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.bytex.snamp.connector.ManagedResourceDescriptionProvider;

/**
 * @author Roman Sakno
 * @version 2.1
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

    private static final LazyReference<GroovyResourceConfigurationDescriptor> INSTANCE = LazyReference.soft();

    private GroovyResourceConfigurationDescriptor(){
        super(new ConnectorConfigurationInfo());
    }

    static GroovyResourceConfigurationDescriptor getInstance(){
        return INSTANCE.get(GroovyResourceConfigurationDescriptor::new);
    }
}
