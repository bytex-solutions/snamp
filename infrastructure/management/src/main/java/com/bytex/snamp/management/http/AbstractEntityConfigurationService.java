package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.EntityConfiguration;
import com.bytex.snamp.configuration.TypedEntityConfiguration;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.management.http.model.AbstractTypedDataObject;
import org.osgi.framework.BundleContext;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstract class for REST services. Contains helper methods for read and write configuration.
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractEntityConfigurationService<E extends TypedEntityConfiguration, DTO extends AbstractTypedDataObject<E>> extends AbstractManagementService {
    final Class<E> entityType;

    AbstractEntityConfigurationService(final Class<E> entityType){
        this.entityType = Objects.requireNonNull(entityType);
    }

    /**
     * Read configuration and return the result
     * @param handler lambda for reading in a appropriate way
     * @param <R> extends EntityConfiguration
     * @return instance of EntityConfiguration
     * @throws WebApplicationException required by configuration admin
     */
     static  <R> R readOnlyActions(final BundleContext context, final Function<? super AgentConfiguration, R> handler) throws WebApplicationException {
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(context, ConfigurationManager.class);
        assert admin != null;
        try {
            return admin.get().transformConfiguration(handler); // readConfiguration ничего не возвращает и в acceptor передать Box нельзя.
        } catch (final IOException exception) {
            throw new WebApplicationException(exception);
        } finally {
            admin.release(context);
        }
    }

    /**
     * Performs the processing of the configuration
     * @param handler lambda for processing the configuration
     * @return empty response with 204 code
     * @throws WebApplicationException required by configuration admin
     */
     static Response changingActions(final BundleContext context, final ConfigurationManager.ConfigurationProcessor<WebApplicationException> handler) throws WebApplicationException {
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(context, ConfigurationManager.class);
        assert admin != null;
        try {
            admin.get().processConfiguration(handler);
        } catch (final IOException exception) {
            throw new WebApplicationException(exception);
        } finally {
            admin.release(context);
        }
        return Response.noContent().build();
    }

    protected abstract DTO toDataTransferObject(final E entity);

    /**
     * Returns all the configured gateways.
     *
     * @return Map that contains configuration (or empty map if no resources are configured)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Map<String, DTO> getAllEntities() {
        return readOnlyActions(getBundleContext(), config -> config.getEntities(entityType)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> toDataTransferObject(entry.getValue()))));
    }

    final <T> T getConfigurationByName(final String name, final Function<? super E, ? extends T> converter){
        return readOnlyActions(getBundleContext(), config -> Optional.ofNullable(config.getEntities(entityType).get(name)))
                .map(converter)
                .orElseThrow(AbstractEntityConfigurationService::notFound);
    }

    /**
     * Returns configuration for certain configured resource by its name.
     *
     * @param name the name
     * @return Map that contains configuration (or empty map if no resources are configured)
     */
    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final DTO getConfigurationByName(@PathParam("name") final String name) {
        return getConfigurationByName(name, this::toDataTransferObject);
    }

    final Response setConfigurationByName(final String name,
                                          final Consumer<? super E> configuration){
        return changingActions(getBundleContext(), config -> {
            configuration.accept(config.getEntities(entityType).getOrAdd(name));
            return true;
        });
    }

    /**
     * Updated certain resource.
     *
     * @param name   the name
     * @param entity the object
     * @return Map that contains configuration (or empty map if no resources are configured)
     */
    @PUT
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response setConfigurationByName(@PathParam("name") final String name,
                                           final DTO entity) {
        return setConfigurationByName(name, entity::exportTo);
    }

    /**
     * Remove gateway from configuration by its name
     *
     * @param name the name
     * @return Map that contains configuration (or empty map if no resources are configured)
     */
    @DELETE
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response removeConfigurationByName(@PathParam("name") final String name) {
        return changingActions(getBundleContext(), config -> {
            if(config.getEntities(entityType).remove(name) == null)
                throw notFound();
            else
                return true;
        });
    }

    /**
     * Returns parameters for certain configured resource by its name.
     *
     * @param name the name
     * @return Map that contains parameters configuration (or empty map if no resources are configured)
     */
    @GET
    @Path("/{name}/parameters")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Map<String, String> getParameters(@PathParam("name") final String name) {
        return readOnlyActions(getBundleContext(), currentConfig -> currentConfig
                .getEntities(entityType)
                .getIfPresent(name)
                .map(EntityConfiguration::getParameters)
                .<WebApplicationException>orElseThrow(AbstractManagementService::notFound));
    }

    /**
     * Set parameters for certain configured resource by its name.
     *
     * @param name   the name
     * @param parameters the object
     * @return no content response
     */
    @PUT
    @Path("/{name}/parameters")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response setParameters(@PathParam("name") final String name, final Map<String, String> parameters) {
        return changingActions(getBundleContext(), config -> {
            config
                    .getEntities(entityType)
                    .getIfPresent(name)
                    .orElseThrow(AbstractManagementService::notFound)
                    .setParameters(parameters);
            return true;
        });
    }

    /**
     * Returns certain parameter for certain configured resource by its name.
     *
     * @param name      the name
     * @param paramName the param name
     * @return String value of the parameter
     */
    @GET
    @Path("/{name}/parameters/{paramName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final String getParameterByName(@PathParam("name") final String name,
                                     @PathParam("paramName") final String paramName) {
        return readOnlyActions(getBundleContext(), currentConfig -> currentConfig
                .getEntities(entityType)
                .getIfPresent(name)
                .filter(entity -> entity.getParameters().containsKey(paramName))
                .map(EntityConfiguration::getParameters)
                .orElseThrow(AbstractManagementService::notFound)
                .get(paramName));
    }

    /**
     * Returns certain parameter for certain configured resource by its name.
     *
     * @param name      the name
     * @param paramName the param name
     * @param value     the value
     * @return no content response
     */
    @PUT
    @Path("/{name}/parameters/{paramName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response setParameterByName(@PathParam("name") final String name,
                                       @PathParam("paramName") final String paramName,
                                       final String value) {
        return changingActions(getBundleContext(), config -> {
            config
                    .getEntities(entityType)
                    .getIfPresent(name)
                    .orElseThrow(AbstractManagementService::notFound)
                    .getParameters()
                    .put(paramName, value);
            return true;
        });
    }

    /**
     * Removes certain parameter for certain configured resource by its name.
     *
     * @param name      the name
     * @param paramName the param name
     * @return no content response
     */
    @DELETE
    @Path("/{name}/parameters/{paramName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response removeParameterByName(@PathParam("name") final String name,
                                          @PathParam("paramName") final String paramName) {
        return changingActions(getBundleContext(), config -> {
            final TypedEntityConfiguration mrc = config.getEntities(entityType).get(name);
            if (mrc == null || mrc.getParameters().remove(paramName) == null) {
                throw notFound();
            } else
                return true;
        });
    }
}