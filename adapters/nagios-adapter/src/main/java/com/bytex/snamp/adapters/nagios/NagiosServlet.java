package com.bytex.snamp.adapters.nagios;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;
import java.util.Objects;

/**
 * Represents a servlet for Nagios REST service.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NagiosServlet extends ServletContainer {
    private static final long serialVersionUID = 6190598618730093687L;

    private static Application createResourceConfig(final NagiosActiveCheckService serviceInstance){
        final DefaultResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(serviceInstance);
        return result;
    }

    NagiosServlet(final NagiosActiveCheckService serviceImpl) {
        super(createResourceConfig(Objects.requireNonNull(serviceImpl)));
    }
}
