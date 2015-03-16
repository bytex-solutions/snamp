package com.itworks.snamp.adapters.http;

import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereObjectFactory;
import org.atmosphere.cpr.AtmosphereServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AtmosphereServletBridge extends AtmosphereServlet {
    private static final long serialVersionUID = 1446026354420742643L;

    private final AtmosphereObjectFactory objectFactory;

    AtmosphereServletBridge(final AtmosphereObjectFactory objectFactory){
        super(false, true);
        this.objectFactory = objectFactory;
    }

    @Override
    protected AtmosphereServletBridge configureFramework(final ServletConfig sc, final boolean init) throws ServletException {
        super.configureFramework(sc, false);
        framework.objectFactory(objectFactory);
        if(init) framework.init(sc);
        return this;
    }

    @Override
    protected AtmosphereFramework newAtmosphereFramework() {
        final AtmosphereFramework result = super.newAtmosphereFramework();
        result.objectFactory(objectFactory);
        return result;
    }
}
