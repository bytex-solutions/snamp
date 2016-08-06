package com.bytex.snamp.connectors.mda.impl;

import com.bytex.snamp.concurrent.LazyValueFactory;
import com.bytex.snamp.concurrent.LazyValue;
import com.bytex.snamp.connectors.mda.MDAResourceConfigurationDescriptorProvider;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class MDAConnectorDescriptionProvider extends MDAResourceConfigurationDescriptorProvider {
    static final String SOCKET_TIMEOUT_PARAM = "socketTimeout";

    private static final class AttributeConfigurationDescriptorImpl extends AttributeConfigurationDescriptor{
        private static final String RESOURCE_NAME = "MdaAttributeConfig";

        private AttributeConfigurationDescriptorImpl(){
            super(RESOURCE_NAME);
        }
    }

    private static final class EventConfigurationDescriptorImpl extends EventConfigurationDescriptor{
        private static final String RESOURCE_NAME = "MdaEventConfig";

        private EventConfigurationDescriptorImpl(){
            super(RESOURCE_NAME);
        }
    }

    private static final class ConnectorConfigurationDescriptorImpl extends ConnectorConfigurationDescriptor{
        private static final String RESOURCE_NAME = "MdaConnectorConfig";

        private ConnectorConfigurationDescriptorImpl(){
            super(RESOURCE_NAME, SOCKET_TIMEOUT_PARAM);
        }
    }

    private static final LazyValue<MDAConnectorDescriptionProvider> INSTANCE = LazyValueFactory.THREAD_SAFE.of(MDAConnectorDescriptionProvider::new);

    private MDAConnectorDescriptionProvider(){
        super(new AttributeConfigurationDescriptorImpl(),
                new EventConfigurationDescriptorImpl(),
                new ConnectorConfigurationDescriptorImpl());
    }

    public static MDAConnectorDescriptionProvider getInstance(){
        return INSTANCE.get();
    }

    public int parseSocketTimeout(final Map<String, String> parameters){
        if(parameters.containsKey(SOCKET_TIMEOUT_PARAM))
            return Integer.parseInt(parameters.get(SOCKET_TIMEOUT_PARAM));
        else return 4000;
    }
}
