package com.itworks.snamp.adapters.http;

import org.atmosphere.cpr.*;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.lang.ref.WeakReference;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AtmosphereServletBridge extends AtmosphereServlet {
    private static final long serialVersionUID = 1446026354420742643L;

    private static final class AtmosphereFrameworkBridge extends AtmosphereFramework{
        private final WeakReference<Servlet> delegatedServlet;

        private AtmosphereFrameworkBridge(final WeakReference<Servlet> delegatedServlet) {
            super(false, true);
            this.delegatedServlet = delegatedServlet;
            //addInitParameter("com.sun.jersey.config.property.packages", "com.itworks.snamp.adapters.http.pkg");
            //addInitParameter("org.atmosphere.websocket.messageContentType", "application/json");
        }

        @Override
        public <T, U extends T> T newClassInstance(final Class<T> classType, final Class<U> defaultType) throws InstantiationException, IllegalAccessException {
            if (classType == Servlet.class) {
                final Servlet servlet = delegatedServlet.get();
                if (servlet != null) return classType.cast(servlet);
            }
            return super.newClassInstance(classType, defaultType);
        }
    }

    private static final class AtmosphereFrameworkInitializerBridge extends AtmosphereFrameworkInitializer{
        private final WeakReference<Servlet> delegatedServlet;

        private AtmosphereFrameworkInitializerBridge(final WeakReference<Servlet> delegatedServlet){
            super(false, true);
            this.delegatedServlet = delegatedServlet;
        }

        @Override
        protected AtmosphereFrameworkBridge newAtmosphereFramework() {
            return new AtmosphereFrameworkBridge(delegatedServlet);
        }
    }

    private final AtmosphereFrameworkInitializerBridge customizedInitializer;

    AtmosphereServletBridge(final Servlet servletImpl){
        super(false, true);
        customizedInitializer = new AtmosphereFrameworkInitializerBridge(new WeakReference<>(servletImpl));
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
