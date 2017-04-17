package com.bytex.snamp.supervision.discovery.rest;

import com.bytex.snamp.ImportClass;
import com.sun.jersey.server.impl.container.servlet.JerseyServletContainerInitializer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ImportClass(JerseyServletContainerInitializer.class)
public final class RESTDiscoveryServlet extends ServletContainer {

}
