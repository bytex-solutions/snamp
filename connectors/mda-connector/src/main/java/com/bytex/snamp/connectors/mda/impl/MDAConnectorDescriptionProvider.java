package com.bytex.snamp.connectors.mda.impl;

import com.bytex.snamp.connectors.ManagedResourceDescriptionProvider;

import java.util.Map;
import static com.bytex.snamp.connectors.mda.impl.MDAResourceConfigurationDescriptorProviderImpl.SOCKET_TIMEOUT_PARAM;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MDAConnectorDescriptionProvider extends ManagedResourceDescriptionProvider {
    public int parseSocketTimeout(final Map<String, String> parameters){
        if(parameters.containsKey(SOCKET_TIMEOUT_PARAM))
            return Integer.parseInt(parameters.get(SOCKET_TIMEOUT_PARAM));
        else return 4000;
    }
}
