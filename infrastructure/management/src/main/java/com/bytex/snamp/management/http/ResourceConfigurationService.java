package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.FeatureDescriptor;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.attributes.AttributeManager;
import com.bytex.snamp.connector.notifications.NotificationManager;
import com.bytex.snamp.connector.operations.OperationManager;
import com.bytex.snamp.management.http.model.*;
import com.google.common.collect.Maps;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Provides API for SNAMP resources management.
 *
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@Path("/configuration/resource")
public final class ResourceConfigurationService extends TemplateConfigurationService<ManagedResourceConfiguration, ResourceDataObject> {
    ResourceConfigurationService() {
        super(EntityMapResolver.RESOURCES);
    }

    @Override
    protected ResourceDataObject toDataTransferObject(final ManagedResourceConfiguration entity) {
        return new ResourceDataObject(entity);
    }

    private static <F extends FeatureConfiguration, DTO extends AbstractFeatureDataObject<F>> Map<String, DTO> transformFeatures(final ClassLoader context,
                                                                                                                                 final Class<F> featureType,
                                                                                                                                 final Map<String, ? extends FeatureDescriptor<F>> descriptors,
                                                                                                                                 final Function<F, DTO> dtoFactory) {
        final Map<String, DTO> result = Maps.newHashMapWithExpectedSize(descriptors.size());
        descriptors.forEach((featureName, descriptor) -> {
            final F config = ConfigurationManager.createEntityConfiguration(context, featureType);
            assert config != null;
            descriptor.fill(config);
            result.put(featureName, dtoFactory.apply(config));
        });
        return result;
    }

    private <F extends FeatureConfiguration, DTO extends AbstractFeatureDataObject<F>> Map<String, DTO> discoverFeatures(final String resourceName,
                                                                                                                         final Class<F> featureType,
                                                                                                                         final Function<? super ManagedResourceConnector, Map<String, ? extends FeatureDescriptor<F>>> discoveryFunction,
                                                                                                                         final Function<F, DTO> dtoFactory) {
        final Optional<ManagedResourceConnectorClient> clientRef = ManagedResourceConnectorClient.tryCreate(getBundleContext(), resourceName);
        if (clientRef.isPresent())
            try (final ManagedResourceConnectorClient connector = clientRef.get()) {
                return transformFeatures(getClass().getClassLoader(), featureType, discoveryFunction.apply(connector), dtoFactory);
            }
        else
            throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @Path("/{resourceName}/discovery/attributes")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, AttributeDataObject> discoverAttributes(@PathParam("resourceName") final String resourceName) {
        return discoverFeatures(resourceName,
                AttributeConfiguration.class,
                connector -> connector.queryObject(AttributeManager.class).map(AttributeManager::discoverAttributes).orElseGet(Collections::emptyMap),
                AttributeDataObject::new);
    }

    @Path("/{resourceName}/discovery/events")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, EventDataObject> discoverEvents(@PathParam("resourceName") final String resourceName) {
        return discoverFeatures(resourceName,
                EventConfiguration.class,
                connector -> connector.queryObject(NotificationManager.class).map(NotificationManager::discoverNotifications).orElseGet(Collections::emptyMap),
                EventDataObject::new);
    }

    @Path("/{resourceName}/discovery/operations")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, OperationDataObject> discoverOperations(@PathParam("resourceName") final String resourceName) {
        return discoverFeatures(resourceName,
                OperationConfiguration.class,
                connector -> connector.queryObject(OperationManager.class).map(OperationManager::discoverOperations).orElseGet(Collections::emptyMap),
                OperationDataObject::new);
    }

    /**
     * Gets connection string.
     *
     * @param resourceName the resource name
     * @return the connection string
     */
    @GET
    @Path("/{name}/connectionString")
    @Produces(MediaType.APPLICATION_JSON)
    public String getConnectionString(@PathParam("name") final String resourceName){
        return getConfigurationByName(resourceName, ManagedResourceConfiguration::getConnectionString);
    }

    /**
     * Sets connection string.
     *
     * @param resourceName the resource name
     * @param value        the value
     * @return the connection string
     */
    @PUT
    @Path("/{name}/connectionString")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setConnectionString(@PathParam("name") final String resourceName, final String value){
        return setConfigurationByName(resourceName, config -> config.setConnectionString(value));
    }

    /**
     * Gets group name.
     *
     * @param resourceName the resource name
     * @return the group name
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{name}/group")
    public String getGroupName(@PathParam("name") final String resourceName){
        return getConfigurationByName(resourceName, ManagedResourceConfiguration::getGroupName);
    }

    /**
     * Sets group name.
     *
     * @param resourceName the resource name
     * @param value        the value
     * @return the group name
     */
    @Path("/{name}/group")
    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setGroupName(@PathParam("name") final String resourceName, final String value){
        return setConfigurationByName(resourceName, config -> config.setGroupName(value));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{name}/overriddenProperties")
    public Set<String> getOverriddenProperties(@PathParam("name") final String resourceName) {
        return getConfigurationByName(resourceName, ManagedResourceConfiguration::getOverriddenProperties);
    }

    @PUT
    @Path("/{name}/overriddenProperties")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setOverriddenProperties(@PathParam("name") final String resourceName, final Set<String> properties) {
        return setConfigurationByName(resourceName, config -> {
            config.getOverriddenProperties().clear();
            config.getOverriddenProperties().addAll(properties);
        });
    }
}
