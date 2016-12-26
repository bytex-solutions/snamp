package com.bytex.snamp.web;

import com.bytex.snamp.web.serviceModel.WebConsoleService;
import org.osgi.framework.ServiceReference;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Represents servlet for {@link com.bytex.snamp.web.serviceModel.WebConsoleService}.
 */
interface WebConsoleServiceReference extends AutoCloseable, Supplier<WebConsoleService> {

    static boolean isResourceModel(final ServiceReference<WebConsoleService> serviceRef) {
        return serviceRef.getProperty(WebConsoleService.URL_CONTEXT) instanceof String;
    }

    static String getName(final ServiceReference<WebConsoleService> serviceRef){
        return Objects.toString(serviceRef.getProperty(WebConsoleService.NAME));
    }

    String getName();

    void activate() throws Exception;
}
