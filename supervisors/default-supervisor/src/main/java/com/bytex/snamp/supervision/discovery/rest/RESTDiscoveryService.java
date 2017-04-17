package com.bytex.snamp.supervision.discovery.rest;

import com.bytex.snamp.configuration.ConfigurationManager;

import javax.annotation.Nonnull;
import javax.ws.rs.Path;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public class RESTDiscoveryService extends DefaultResourceDiscoveryService {
    public RESTDiscoveryService(@Nonnull final String groupName,
                                @Nonnull final ConfigurationManager configManager) {
        super(groupName, configManager);
    }
}
