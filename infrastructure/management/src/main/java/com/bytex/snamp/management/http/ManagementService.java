package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.core.AbstractSnampManager;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.gateway.GatewayClient;
import com.bytex.snamp.management.ManagementUtils;
import com.bytex.snamp.management.SnampManagerImpl;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ManagedResourceConfigurationDTO
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class ManagementService extends AbstractManagementService {
    private static final String INTERNAL_COMPONENT_TYPE_NAME = "Internal component";
    private final AbstractSnampManager manager = new SnampManagerImpl();

    private Map<String, String> stringifyDescription(final ConfigurationEntityDescription.ParameterDescription description) {
        return ImmutableMap.<String, String>builder()
                .put("name", description.getName())
                .put("required", String.valueOf(description.isRequired()))
                .put("defaultValue", description.getDefaultValue(null))
                .put("pattern", description.getValuePattern(null))
                .put("association", Joiner.on(",").skipNulls().join(
                        description.getRelatedParameters
                                (ConfigurationEntityDescription.ParameterRelationship.ASSOCIATION)
                        )
                )
                .put("exclusion", Joiner.on(",").skipNulls().join(
                        description.getRelatedParameters
                                (ConfigurationEntityDescription.ParameterRelationship.EXCLUSION)
                        )
                )
                .put("extension", Joiner.on(",").skipNulls().join(
                        description.getRelatedParameters
                                (ConfigurationEntityDescription.ParameterRelationship.EXTENSION)
                        )
                )
                .build();
    }

    /**
     * Returns all the snamp bundles.
     *
     * @return Map that contains configuration (or empty map if no resources are configured)
     */
    @GET
    @Path("/components")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection getInstalledComponents() {
        final Collection<Map<String, String>> collection = manager.getInstalledComponents()
                .stream()
                .filter(entry -> entry.getName(Locale.getDefault()) != null)
                .map(entry -> ImmutableMap.<String, String>builder()
                        .put("name", entry.getName(Locale.getDefault()))
                        .put("description", entry.toString(Locale.getDefault()))
                        .put("state", ManagementUtils.getStateString(entry))
                        .put("version", entry.getVersion().toString())
                        .put("class", "internal")
                        .put("type", INTERNAL_COMPONENT_TYPE_NAME)
                        .build())
                .collect(Collectors.toList());
        collection.addAll(getInstalledGateways());
        collection.addAll(getInstalledResources());
        return collection;
    }


    /**
     * Gets installed resources.
     *
     * @return the installed resources
     */
    @GET
    @Path("/resource/list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<Map<String, String>> getInstalledResources() {
        return manager.getInstalledResourceConnectors()
                .stream()
                .filter(entry -> entry.getName(Locale.getDefault()) != null)
                .map(entry -> ImmutableMap.<String,String>builder()
                        .put("name", entry.getName(Locale.getDefault()))
                        .put("description", entry.toString(Locale.getDefault()))
                        .put("state", ManagementUtils.getStateString(entry))
                        .put("version", entry.getVersion().toString())
                        .put("class", "resource")
                        .put("type", entry.getType())
                        .build())
                .collect(Collectors.toList());
    }


    /**
     * Gets installed gateways.
     *
     * @return the installed gateways
     */
    @GET
    @Path("/gateway/list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<Map<String, String>> getInstalledGateways() {
        return manager.getInstalledGateways()
                .stream()
                .filter(entry -> entry.getName(Locale.getDefault()) != null)
                .map(entry -> ImmutableMap.<String,String>builder()
                        .put("name", entry.getName(Locale.getDefault()))
                        .put("description", entry.toString(Locale.getDefault()))
                        .put("state", ManagementUtils.getStateString(entry))
                        .put("version", entry.getVersion().toString())
                        .put("class", "gateway")
                        .put("type", entry.getType())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Restart all the system.
     *
     * @return the response
     */
    @GET
    @Path("/restart")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response restartAllTheSystem() {
        try {
            SnampManagerImpl.restart(getBundleContext());
        } catch (final BundleException e) {
            throw new WebApplicationException(e);
        }
        return Response.noContent().build();
    }

    /**
     * Stop resource.
     *
     * @param name the name
     * @return the boolean
     */
    @POST
    @Path("/resource/{name}/disable")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean disableConnector(@PathParam("name") final String name)  {
        try {
            return ManagedResourceActivator.disableConnector(getBundleContext(), name);
        } catch (final BundleException e) {
            throw new WebApplicationException(e);
        }
    }

    /**
     * Start resource.
     *
     * @param name the name
     * @return the boolean
     */
    @POST
    @Path("/resource/{name}/enable")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean enableConnector(@PathParam("name") final String name)  {
        try {
            return ManagedResourceActivator.enableConnector(getBundleContext(), name);
        } catch (final BundleException e) {
            throw new WebApplicationException(e);
        }
    }


    /**
     * Stop gateway.
     *
     * @param name the name
     * @return the boolean
     */
    @POST
    @Path("/gateway/{name}/disable")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean disableGateway(@PathParam("name") final String name)  {
        try {
            return GatewayActivator.disableGateway(getBundleContext(), name);
        } catch (final BundleException e) {
            throw new WebApplicationException(e);
        }
    }

    /**
     * Start gateway.
     *
     * @param name the name
     * @return the boolean
     */
    @POST
    @Path("/gateway/{name}/enable")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean enableGateway(@PathParam("name") final String name)  {
        try {
            return GatewayActivator.enableGateway(getBundleContext(), name);
        } catch (final BundleException e) {
            throw new WebApplicationException(e);
        }
    }


    /**
     * Gets gateway description.
     *
     * @param name the name
     * @return the entity description
     */
    @GET
    @Path("/gateway/{name}/configuration")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<Map<String, String>> getGatewayDescription(@PathParam("name") final String name) {
        try {
            final ConfigurationEntityDescription<GatewayConfiguration> descriptor =
                    GatewayClient.getConfigurationEntityDescriptor(getBundleContext(), name,
                            GatewayConfiguration.class);
            return descriptor == null ? Collections.EMPTY_LIST : descriptor.stream()
                    .map(entry -> stringifyDescription(descriptor.getParameterDescriptor(entry)))
                    .collect(Collectors.toList());
        } catch (final UnsupportedOperationException exception) {
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * Gets managed resource description.
     *
     * @param name the name
     * @return the entity description
     */
    @GET
    @Path("/resource/{name}/configuration")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<Map<String, String>> getResourceDescription(@PathParam("name") final String name) {
        try {
            final ConfigurationEntityDescription<ManagedResourceConfiguration> descriptor =
                    ManagedResourceConnectorClient.getConfigurationEntityDescriptor(getBundleContext(), name,
                            ManagedResourceConfiguration.class);
            return descriptor == null ? Collections.EMPTY_LIST : descriptor.stream()
                    .map(entry -> stringifyDescription(descriptor.getParameterDescriptor(entry)))
                    .collect(Collectors.toList());
        } catch (final UnsupportedOperationException exception) {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Gets resource attribute description.
     *
     * @param name the name
     * @return the resource attribute description
     */
    @GET
    @Path("/resource/{name}/attribute/configuration")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<Map<String, String>> getResourceAttributeDescription(@PathParam("name") final String name) {
        try {
            final ConfigurationEntityDescription<AttributeConfiguration> descriptor =
                    ManagedResourceConnectorClient.getConfigurationEntityDescriptor(getBundleContext(), name,
                            AttributeConfiguration.class);
            return descriptor == null ? Collections.EMPTY_LIST : descriptor.stream()
                    .map(entry -> stringifyDescription(descriptor.getParameterDescriptor(entry)))
                    .collect(Collectors.toList());
        } catch (final UnsupportedOperationException exception) {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Gets resource event description.
     *
     * @param name the name
     * @return the resource event description
     */
    @GET
    @Path("/resource/{name}/event/configuration")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<Map<String, String>> getResourceEventDescription(@PathParam("name") final String name) {
        try {
            final ConfigurationEntityDescription<EventConfiguration> descriptor =
                    ManagedResourceConnectorClient.getConfigurationEntityDescriptor(getBundleContext(), name,
                            EventConfiguration.class);
            return descriptor == null ? Collections.EMPTY_LIST : descriptor.stream()
                    .map(entry -> stringifyDescription(descriptor.getParameterDescriptor(entry)))
                    .collect(Collectors.toList());
        } catch (final UnsupportedOperationException exception) {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Gets resource operation description.
     *
     * @param name the name
     * @return the resource operation description
     */
    @GET
    @Path("/resource/{name}/operation/configuration")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<Map<String, String>> getResourceOperationDescription(@PathParam("name") final String name) {
        try {
            final ConfigurationEntityDescription<OperationConfiguration> descriptor =
                    ManagedResourceConnectorClient.getConfigurationEntityDescriptor(getBundleContext(), name,
                            OperationConfiguration.class);
            return descriptor == null ? Collections.EMPTY_LIST : descriptor.stream()
                    .map(entry -> stringifyDescription(descriptor.getParameterDescriptor(entry)))
                    .collect(Collectors.toList());
        } catch (final UnsupportedOperationException exception) {
            return Collections.EMPTY_LIST;
        }
    }
}
