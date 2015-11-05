package com.bytex.snamp.connectors.mda.impl;

import com.bytex.snamp.connectors.mda.MDAResourceConfigurationDescriptorProvider;
import com.bytex.snamp.core.ServiceSpinWait;
import com.hazelcast.core.HazelcastInstance;
import org.osgi.framework.BundleContext;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MDAResourceConfigurationDescriptorProviderImpl extends MDAResourceConfigurationDescriptorProvider {
    private static final String SOCKET_TIMEOUT_PARAM = "socketTimeout";
    private static final String WAIT_FOR_HZ_PARAM = "waitForHazelcast";

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
            super(RESOURCE_NAME,  WAIT_FOR_HZ_PARAM, SOCKET_TIMEOUT_PARAM);
        }
    }

    MDAResourceConfigurationDescriptorProviderImpl(){
        super(new AttributeConfigurationDescriptorImpl(),
                new EventConfigurationDescriptorImpl(),
                new ConnectorConfigurationDescriptorImpl());
    }

    public static int parseSocketTimeout(final Map<String, String> parameters){
        if(parameters.containsKey(SOCKET_TIMEOUT_PARAM))
            return Integer.parseInt(parameters.get(SOCKET_TIMEOUT_PARAM));
        else return 4000;
    }

    public static boolean waitForHazelcast(final Map<String, String> parameters, final BundleContext context) throws TimeoutException, InterruptedException {
        if(parameters.containsKey(WAIT_FOR_HZ_PARAM)){
            final long timeout = Long.parseLong(parameters.get(WAIT_FOR_HZ_PARAM));
            final ServiceSpinWait<HazelcastInstance> hazelcastWait = new ServiceSpinWait<>(context, HazelcastInstance.class);
            try {
                return hazelcastWait.get(timeout, TimeUnit.MILLISECONDS) != null;
            } catch (final ExecutionException ignored) {
                return false;
            }
        }
        else return false;
    }
}
