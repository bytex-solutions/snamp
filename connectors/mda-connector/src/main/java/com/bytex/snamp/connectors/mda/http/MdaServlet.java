package com.bytex.snamp.connectors.mda.http;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class MdaServlet extends ServletContainer {
    private static final long serialVersionUID = 6190598618730093687L;

    private static Application createResourceConfig(final HttpDataAcceptor serviceInstance){
        final DefaultResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(serviceInstance);
        return result;
    }

    public MdaServlet(final HttpDataAcceptor serviceImpl) {
        super(createResourceConfig(Objects.requireNonNull(serviceImpl)));
    }
}
