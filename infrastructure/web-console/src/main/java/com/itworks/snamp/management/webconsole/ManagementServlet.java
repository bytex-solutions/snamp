package com.itworks.snamp.management.webconsole;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */

import com.itworks.snamp.configuration.PersistentConfigurationManager;
import com.itworks.snamp.management.SnampManager;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Represents descriptor of the HTTP servlet container.
 * @author Roman Sakno
 */
final class ManagementServlet extends ServletContainer {

    private static ResourceConfig createResourceConfig(final ManagementServiceImpl serviceInstance){
        final ResourceConfig result = new ResourceConfig();
        result.register(serviceInstance);
        return result;
    }

    /**
     * Initializes a new instance of the rest service.
     */
    ManagementServlet(final PersistentConfigurationManager configManager,
                             final SnampManager snampManager){
        super(createResourceConfig(new ManagementServiceImpl(configManager, snampManager)));
    }
}
