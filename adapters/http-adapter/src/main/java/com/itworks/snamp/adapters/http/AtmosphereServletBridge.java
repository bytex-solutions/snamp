package com.itworks.snamp.adapters.http;

import org.atmosphere.cpr.*;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AtmosphereServletBridge extends AtmosphereServlet {
    private static final long serialVersionUID = 1446026354420742643L;

    private static final class AtmosphereFrameworkBridge extends AtmosphereFramework{
        private final ServletFactory<?> servletFactory;

        private AtmosphereFrameworkBridge(final ServletFactory<?> servletFactory) {
            super(false, true);
            this.servletFactory = servletFactory;
            //addInitParameter("com.sun.jersey.config.property.packages", "com.itworks.snamp.adapters.http.pkg");
            //addInitParameter("org.atmosphere.websocket.messageContentType", "application/json");
        }

        @Override
        public <T, U extends T> T newClassInstance(final Class<T> classType, final Class<U> defaultType) throws InstantiationException, IllegalAccessException {
            return classType == Servlet.class ?
                    classType.cast(servletFactory.get()) :
                    super.newClassInstance(classType, defaultType);
        }
    }

    private static final class AtmosphereFrameworkInitializerBridge extends AtmosphereFrameworkInitializer{
        private final ServletFactory<?> servletFactory;

        private AtmosphereFrameworkInitializerBridge(final ServletFactory<?> servletFactory){
            super(false, true);
            this.servletFactory = servletFactory;
        }

        @Override
        protected AtmosphereFrameworkBridge newAtmosphereFramework() {
            return new AtmosphereFrameworkBridge(servletFactory);
        }
    }

    private final AtmosphereFrameworkInitializerBridge customizedInitializer;

    <S extends Servlet> AtmosphereServletBridge(final ServletFactory<S> servletFactory){
        super(false, true);
        customizedInitializer = new AtmosphereFrameworkInitializerBridge(servletFactory);
    }

    @Override
    protected AtmosphereServlet configureFramework(final ServletConfig sc, final boolean init) throws ServletException {
        customizedInitializer.configureFramework(sc, init, false);
        framework = customizedInitializer.framework();
        return this;
    }

    @Override
    protected AtmosphereFrameworkBridge newAtmosphereFramework() {
        final AtmosphereFrameworkBridge result = customizedInitializer.newAtmosphereFramework();
        framework = result;
        return result;
    }

    @Override
    public AtmosphereFramework framework() {
        return framework;
    }

    @Override
    public void destroy() {
        try {
            customizedInitializer.destroy();
        }
        finally {
            framework = null;
        }
    }

    static BroadcasterConfig createBroadcasterConfig(final AtmosphereConfig config,
                                                     final String id) {
        return new BroadcasterConfig(config.framework().broadcasterFilters(), config, id)
                .init();
    }
}
