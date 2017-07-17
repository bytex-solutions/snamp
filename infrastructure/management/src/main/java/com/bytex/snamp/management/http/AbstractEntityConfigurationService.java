package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.EntityConfiguration;
import com.bytex.snamp.configuration.EntityMapResolver;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.management.http.model.AbstractDataObject;
import org.osgi.framework.BundleContext;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
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
public abstract class AbstractEntityConfigurationService<E extends EntityConfiguration, DTO extends AbstractDataObject<E>> extends AbstractManagementService {
    final EntityMapResolver<AgentConfiguration, E> entityMapResolver;

    AbstractEntityConfigurationService(final EntityMapResolver<AgentConfiguration, E> resolver){
        entityMapResolver = Objects.requireNonNull(resolver);
    }

    private static WebApplicationException configurationManagerIsMissing(){
        return new WebApplicationException(new AssertionError("Configuration manager is not available"));
    }

    /**
     * Read configuration and return the result
     * @param handler lambda for reading in a appropriate way
     * @param <R> extends EntityConfiguration
     * @return instance of EntityConfiguration
     * @throws WebApplicationException required by configuration admin
     */
     static  <R> R readOnlyActions(final BundleContext context, final Function<? super AgentConfiguration, R> handler) throws WebApplicationException {
         return ServiceHolder.tryCreate(context, ConfigurationManager.class)
                 .map(admin -> {
                     try {
                         return admin.get().transformConfiguration(handler); // readConfiguration ничего не возвращает и в acceptor передать Box нельзя.
                     } catch (final IOException exception) {
                         throw new WebApplicationException(exception);
                     } finally {
                         admin.release(context);
                     }
                 }).orElseThrow(AbstractEntityConfigurationService::configurationManagerIsMissing);
     }

    /**
     * Performs the processing of the configuration
     * @param context Context of the calling bundle.
     * @param handler lambda for processing the configuration
     * @return empty response with 204 code
     * @throws WebApplicationException required by configuration admin
     */
     static Response changingActions(final BundleContext context,
                                     final ConfigurationManager.ConfigurationProcessor<WebApplicationException> handler) throws WebApplicationException {
         ServiceHolder.tryCreate(context, ConfigurationManager.class).ifPresent(admin -> {
             try {
                 admin.get().processConfiguration(handler);
             } catch (final IOException exception) {
                 throw new WebApplicationException(exception);
             } finally {
                 admin.release(context);
             }
         });
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
        return readOnlyActions(getBundleContext(), config -> entityMapResolver.apply(config)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> toDataTransferObject(entry.getValue()))));
    }

    final <T> T getConfigurationByName(final String name, final Function<? super E, ? extends T> converter) {
        return readOnlyActions(getBundleContext(), config -> Optional.ofNullable(entityMapResolver.apply(config).get(name)))
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
            configuration.accept(entityMapResolver.apply(config).getOrAdd(name));
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
            if(entityMapResolver.apply(config).remove(name) == null)
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
        return readOnlyActions(getBundleContext(), currentConfig -> entityMapResolver.apply(currentConfig)
                .getIfPresent(name)
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
    public final Response setParameters(@PathParam("name") final String name,
                                        final Map<String, String> parameters) {
        return changingActions(getBundleContext(), config -> {
                    entityMapResolver.apply(config)
                    .getIfPresent(name)
                    .orElseThrow(AbstractManagementService::notFound)
                    .load(parameters);
            return true;
        });
    }

    /**
     * Remove parameters for certain configured resource by its name.
     *
     * @param name   the name
     * @return no content response
     */
    @DELETE
    @Path("/{name}/parameters")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response removeParameters(@PathParam("name") final String name) {
        return changingActions(getBundleContext(), config -> {
            entityMapResolver.apply(config)
                    .getIfPresent(name)
                    .orElseThrow(AbstractManagementService::notFound)
                    .load(Collections.emptyMap());
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
        return readOnlyActions(getBundleContext(), currentConfig -> entityMapResolver.apply(currentConfig)
                .getIfPresent(name)
                .filter(entity -> entity.containsKey(paramName))
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
                    entityMapResolver.apply(config)
                    .getIfPresent(name)
                    .orElseThrow(AbstractManagementService::notFound)
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
        return changingActions(getBundleContext(), config -> entityMapResolver.apply(config)
                .getIfPresent(name)
                .orElseThrow(AbstractManagementService::notFound)
                .remove(paramName) != null);
    }
}
