package com.bytex.snamp.supervision.def;

import com.bytex.snamp.supervision.discovery.ResourceDiscoveryException;

/**
 * Indicates that the {@link com.bytex.snamp.configuration.ConfigurationManager} service is not available
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ConfigurationManagerIsNotAvailable extends ResourceDiscoveryException {
    ConfigurationManagerIsNotAvailable(){
        super("ConfigurationManager is not available through OSGi Service Registry");
    }
}
