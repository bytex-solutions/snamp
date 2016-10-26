package com.bytex.snamp.connector.mda.impl;

import com.bytex.snamp.LazyValueFactory;
import com.bytex.snamp.LazyValue;
import com.bytex.snamp.connector.mda.MDAResourceConfigurationDescriptorProvider;

import java.util.Map;
import static com.bytex.snamp.MapUtils.getValueAsInt;

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
        return getValueAsInt(parameters, SOCKET_TIMEOUT_PARAM, Integer::parseInt, () -> 4000);
    }
}
