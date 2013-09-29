package com.snamp.hosting;

import com.snamp.ExtensionsManager;
import com.snamp.connectors.ManagementConnector;
import com.snamp.connectors.ManagementConnectorFactory;

import java.util.logging.Logger;

/**
 * Represents internal hosting services and routines.
 * @author roman
 */
final class HostingServices {
    private static final Logger log = Logger.getLogger("snamp.log");

    private HostingServices(){

    }

    public static ManagementConnector createConnector(final AgentConfiguration.ManagementTargetConfiguration target){
        if(target == null) throw new IllegalArgumentException("target is null.");
        final ManagementConnectorFactory factory = ExtensionsManager.getManagementConnectorFactory(target.getConnectionType());
        if(factory == null){
            log.warning(String.format("Unsupported management connector '%s'", target.getConnectionType()));
            return null;
        }
        return factory.newInstance(target.getConnectionString(), target.getAdditionalElements());
    }
}
