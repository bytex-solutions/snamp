package com.itworks.snamp.management.webconsole;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */

import com.itworks.snamp.configuration.ConfigurationManager;
import com.itworks.snamp.management.SnampManager;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;

/**
 * Represents descriptor of the HTTP servlet container.
 * @author Roman Sakno
 */
final class ManagementServlet extends ServletContainer {

    private static Application createResourceConfig(final ManagementServiceImpl serviceInstance){
        final DefaultResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(serviceInstance);
        return result;
    }

    /**
     * Initializes a new instance of the rest service.
     */
    public ManagementServlet(final ConfigurationManager configManager,
                             final SnampManager snampManager){
        super(createResourceConfig(new ManagementServiceImpl(configManager, snampManager)));
    }
}
