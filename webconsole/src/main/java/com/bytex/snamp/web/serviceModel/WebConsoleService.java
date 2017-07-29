package com.bytex.snamp.web.serviceModel;

import com.bytex.snamp.core.DefaultServiceSelector;
import org.osgi.framework.ServiceReference;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a service for SNAMP Web Console.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface WebConsoleService extends AutoCloseable {
    String SERVICE_NAME_PROPERTY = "webConsoleServiceName";
    
    void attachSession(final WebConsoleSession session);

    static Optional<String> getServiceName(final ServiceReference<? extends WebConsoleService> serviceRef) {
        return Optional.ofNullable(serviceRef.getProperty(SERVICE_NAME_PROPERTY)).map(Objects::toString);
    }

    static DefaultServiceSelector createSelector(){
        return new DefaultServiceSelector()
                .setServiceType(WebConsoleService.class);
    }
}
