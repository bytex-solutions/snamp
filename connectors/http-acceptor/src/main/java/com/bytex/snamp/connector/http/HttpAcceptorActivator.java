package com.bytex.snamp.connector.http;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;
import org.osgi.service.http.HttpService;

import java.util.Collection;
import java.util.Map;

/**
 * Represents activator of {@link HttpAcceptor}.
 */
public final class HttpAcceptorActivator extends ManagedResourceActivator<HttpAcceptor> {

    @SpecialUse
    public HttpAcceptorActivator() {
        super(HttpAcceptorActivator::newResourceConnector);
    }

    private static HttpAcceptor newResourceConnector(final String resourceName,
                                              final String connectionString,
                                              final Map<String, String> parameters,
                                              final RequiredService<?>... dependencies) throws Exception{
        return new HttpAcceptor(resourceName, parameters);
    }

    @Override
    protected void addDependencies(final Collection<RequiredService<?>> dependencies) {
        dependencies.add(new SimpleDependency<>(HttpService.class));
    }

    @Override
    protected void activate(final RequiredService<?>... dependencies) throws Exception {
        getLogger().info("Starting global HTTP acceptor");
    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     */
    @Override
    protected void deactivate(final ActivationPropertyReader activationProperties) {
        getLogger().info("Shutdown global HTTP acceptor");
    }
}
