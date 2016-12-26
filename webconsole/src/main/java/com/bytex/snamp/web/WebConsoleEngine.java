package com.bytex.snamp.web;

import com.bytex.snamp.core.FrameworkService;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import org.osgi.framework.ServiceListener;

import javax.servlet.Servlet;
import java.util.Collection;

/**
 * Represents WebConsole as OSGi service.
 */
public interface WebConsoleEngine extends FrameworkService, Servlet, ServiceListener {
    /**
     * Gets immutable collection of all registered additional services for Web Console.
     * @return Immutable collection of registered services.
     */
    Collection<WebConsoleService> registeredServices();
}
