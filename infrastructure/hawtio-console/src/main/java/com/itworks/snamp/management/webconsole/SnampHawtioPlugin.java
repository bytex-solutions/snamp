package com.itworks.snamp.management.webconsole;

import io.hawt.web.plugin.HawtioPlugin;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Plugin context listener.
 *
 * @author Evgeniy Kirichenko
 */
public final class SnampHawtioPlugin extends HawtioPlugin implements ServletContextListener {

//org.jolokia.http.HttpRequestHandler


    @Override
    public synchronized void contextInitialized(final ServletContextEvent servletContextEvent) {
        final ServletContext context = servletContextEvent.getServletContext();

        setContext(context.getInitParameter("plugin-context"));
        setName(context.getInitParameter("plugin-name"));
        setScripts(context.getInitParameter("plugin-scripts"));
        setDomain(null);
        init();
    }

    @Override
    public synchronized void contextDestroyed(final ServletContextEvent servletContextEvent) {
        destroy();
    }
}
