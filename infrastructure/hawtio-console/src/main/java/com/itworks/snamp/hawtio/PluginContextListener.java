package com.itworks.snamp.hawtio;

import io.hawt.web.plugin.HawtioPlugin;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Plugin context listener.
 *
 * @author Evgeniy Kirichenko
 */
public class PluginContextListener implements ServletContextListener {


    HawtioPlugin plugin = null;


    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        final ServletContext context = servletContextEvent.getServletContext();
        System.err.println("Plugin context: " + context.getInitParameter("plugin-context"));
        System.err.println("Plugin name: " + context.getInitParameter("plugin-name"));
        System.err.println("Plugin scripts: " + context.getInitParameter("plugin-scripts"));

        plugin = new HawtioPlugin();
        plugin.setContext(context.getInitParameter("plugin-context"));
        plugin.setName(context.getInitParameter("plugin-name"));
        plugin.setScripts(context.getInitParameter("plugin-scripts"));
        plugin.setDomain(null);
        plugin.init();

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        plugin.destroy();
    }
}
