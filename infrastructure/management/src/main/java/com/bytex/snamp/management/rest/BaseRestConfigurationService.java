package com.bytex.snamp.management.rest;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import org.osgi.framework.BundleContext;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.function.Function;

/**
 * Abstract class for REST services. Contains helper methods for read and write configuration.
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public abstract class BaseRestConfigurationService {
    /**
     * Read configuration and return the result
     * @param handler lambda for reading in a appropriate way
     * @param <R> extends EntityConfiguration
     * @return instance of EntityConfiguration
     * @throws WebApplicationException required by configuration admin
     */
     final <R> R readOnlyActions(final Function<? super AgentConfiguration, R> handler) throws WebApplicationException {
        final BundleContext bc = Utils.getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(bc, ConfigurationManager.class);
        assert admin != null;
        try {
            return admin.get().transformConfiguration(handler); // readConfiguration ничего не возвращает и в acceptor передать Box нельзя.
        } catch (final IOException exception) {
            throw new WebApplicationException(exception);
        } finally {
            admin.release(bc);
        }
    }

    /**
     * Performs the processing of the configuration
     * @param handler lambda for processing the configuration
     * @return empty response with 204 code
     * @throws WebApplicationException required by configuration admin
     */
     final Response changingActions(final ConfigurationManager.ConfigurationProcessor<WebApplicationException> handler) throws WebApplicationException {
        final BundleContext bc = Utils.getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(bc, ConfigurationManager.class);
        assert admin != null;
        try {
            admin.get().processConfiguration(handler);
        } catch (final IOException exception) {
            throw new WebApplicationException(exception);
        } finally {
            admin.release(bc);
        }
        return Response.noContent().build();
    }
}
