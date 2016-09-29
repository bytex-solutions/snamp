package com.bytex.snamp.connector.groovy.impl;

import com.bytex.snamp.concurrent.LazyValue;
import com.bytex.snamp.concurrent.LazyValueFactory;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import java.util.Map;

import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.connector.ManagedResourceDescriptionProvider;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class GroovyResourceConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl implements ManagedResourceDescriptionProvider {
    private static final String INIT_SCRIPT_PARAM = "initScript";

    static String getInitScriptFile(final Map<String, String> parameters){
        return parameters.get(INIT_SCRIPT_PARAM);
    }

    private static final class ConnectorConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        private static final String RESOURCE_NAME = "ConnectorParameters";

        private ConnectorConfigurationInfo(){
            super(RESOURCE_NAME,
                    ManagedResourceConfiguration.class,
                    INIT_SCRIPT_PARAM,
                    "groovy.warnings",
                    "groovy.source.encoding",
                    "groovy.classpath",
                    "groovy.output.verbose",
                    "groovy.output.debug",
                    "groovy.errors.tolerance");
        }
    }

    private static final LazyValue<GroovyResourceConfigurationDescriptor> INSTANCE = LazyValueFactory.THREAD_SAFE_SOFT_REFERENCED.of(GroovyResourceConfigurationDescriptor::new);

    private GroovyResourceConfigurationDescriptor(){
        super(new ConnectorConfigurationInfo());
    }

    static GroovyResourceConfigurationDescriptor getInstance(){
        return INSTANCE.get();
    }
}
