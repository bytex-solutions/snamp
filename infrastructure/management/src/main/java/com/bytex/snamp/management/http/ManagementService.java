package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.core.AbstractSnampManager;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.core.SnampComponentDescriptor;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.gateway.GatewayClient;
import com.bytex.snamp.management.DefaultSnampManager;
import com.bytex.snamp.management.ManagementUtils;
import com.bytex.snamp.management.http.model.AgentDataObject;
import com.bytex.snamp.supervision.SupervisorActivator;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
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
    @FunctionalInterface
    private interface EntityDescriptorProvider<T extends EntityConfiguration>{
        ConfigurationEntityDescription<T> getDescriptor(final BundleContext context, final String componentName, final Class<T> entityType);
    }

    private static final String INTERNAL_COMPONENT_TYPE_NAME = "Internal component";
    private final AbstractSnampManager manager = new DefaultSnampManager();

    private static Map<String, String> stringifyDescription(final ConfigurationEntityDescription.ParameterDescription description) {
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

    private static String getComponentSystemType(final SnampComponentDescriptor component){
        if(component.containsKey(SnampComponentDescriptor.CONNECTOR_TYPE_PROPERTY))
            return "connectors";
        else if(component.containsKey(SnampComponentDescriptor.GATEWAY_TYPE_PROPERTY))
            return "gateways";
        else if(component.containsKey(SnampComponentDescriptor.SUPERVISOR_TYPE_PROPERTY))
            return "supervisors";
        else
            return INTERNAL_COMPONENT_TYPE_NAME;
    }

    private <T extends SnampComponentDescriptor> void fillInstalledComponents(final Function<? super AbstractSnampManager, Collection<? extends T>> componentInfoProvider,
                                                                              final Function<? super T, String> typeResolver,
                                                                              final Collection<Map<String, String>> output) {
        componentInfoProvider.apply(manager)
                .stream()
                .filter(entry -> entry.getName(Locale.getDefault()) != null)
                .map(entry -> ImmutableMap.<String, String>builder()
                        .put("name", entry.getName(Locale.getDefault()))
                        .put("description", entry.toString(Locale.getDefault()))
                        .put("state", ManagementUtils.getStateString(entry))
                        .put("version", entry.getVersion().toString())
                        .put("class", getComponentSystemType(entry))
                        .put("type", typeResolver.apply(entry))
                        .build())
                .forEach(output::add);
    }

    private void fillInstalledConnectors(final Collection<Map<String, String>> output) {
        fillInstalledComponents(AbstractSnampManager::getInstalledResourceConnectors,
                AbstractSnampManager.ResourceConnectorDescriptor::getType,
                output);
    }

    private void fillInstalledGateways(final Collection<Map<String, String>> output) {
        fillInstalledComponents(AbstractSnampManager::getInstalledGateways,
                AbstractSnampManager.GatewayDescriptor::getType,
                output);
    }

    private void fillInstalledSupervisors(final Collection<Map<String, String>> output) {
        fillInstalledComponents(AbstractSnampManager::getInstalledSupervisors,
                AbstractSnampManager.SupervisorDescriptor::getType,
                output);
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
    public Collection<Map<String, String>> getInstalledComponents() {
        final Collection<Map<String, String>> collection = new LinkedList<>();
        fillInstalledComponents(AbstractSnampManager::getInstalledComponents, c -> INTERNAL_COMPONENT_TYPE_NAME, collection);
        fillInstalledConnectors(collection);
        fillInstalledGateways(collection);
        fillInstalledSupervisors(collection);
        return collection;
    }



    /**
     * Gets installed resources.
     *
     * @return the installed resources
     */
    @GET
    @Path("/components/connectors")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Map<String, String>> getInstalledConnectors() {
        final Collection<Map<String, String>> collection = new LinkedList<>();
        fillInstalledConnectors(collection);
        return collection;
    }


    /**
     * Gets installed gateways.
     *
     * @return the installed gateways
     */
    @GET
    @Path("/components/gateways")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Map<String, String>> getInstalledGateways() {
        final Collection<Map<String, String>> collection = new LinkedList<>();
        fillInstalledGateways(collection);
        return collection;
    }

    /**
     * Gets installed supervisors
     * @return the installed supervisors.
     */
    @GET
    @Path("/components/supervisors")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Map<String, String>> getInstalledSupervisors(){
        final Collection<Map<String, String>> collection = new LinkedList<>();
        fillInstalledSupervisors(collection);
        return collection;
    }

    /**
     * Restart all the system.
     *
     * @return the response
     */
    @GET
    @Path("/restart")
    @Produces(MediaType.APPLICATION_JSON)
    public Response restartAllTheSystem() throws BundleException {
        DefaultSnampManager.restart(getBundleContext());
        return Response.noContent().build();
    }

    /**
     * Stop resource.
     *
     * @param connectorType the name
     * @return the boolean
     */
    @POST
    @Path("/components/connectors/{type}/disable")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean disableConnector(@PathParam("type") final String connectorType) throws BundleException {
        return ManagedResourceActivator.disableConnector(getBundleContext(), connectorType);
    }

    /**
     * Start resource.
     *
     * @param connectorType the name
     * @return the boolean
     */
    @POST
    @Path("/components/connectors/{type}/enable")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean enableConnector(@PathParam("type") final String connectorType) throws BundleException {
        return ManagedResourceActivator.enableConnector(getBundleContext(), connectorType);
    }

    /**
     * Stop supervisor.
     *
     * @param supervisorType the name
     * @return the boolean
     */
    @POST
    @Path("/components/supervisors/{type}/disable")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean disableSupervisor(@PathParam("type") final String supervisorType) throws BundleException {
        switch (supervisorType) {
            case SupervisorConfiguration.DEFAULT_TYPE:
                throw new WebApplicationException(Response
                        .status(Response.Status.BAD_REQUEST)
                        .entity(String.format("Supervisor %s is reserved and cannot be stopped", supervisorType))
                        .build());
            default:
                return SupervisorActivator.disableSupervsior(getBundleContext(), supervisorType);
        }
    }

    /**
     * Start supervisor.
     *
     * @param supervisorType the name
     * @return the boolean
     */
    @POST
    @Path("/components/supervisors/{type}/enable")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean enableSupervisor(@PathParam("type") final String supervisorType) throws BundleException {
        return SupervisorActivator.enableSupervisor(getBundleContext(), supervisorType);
    }

    /**
     * Stop gateway.
     *
     * @param gatewayType the name
     * @return the boolean
     */
    @POST
    @Path("/components/gateways/{type}/disable")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean disableGateway(@PathParam("type") final String gatewayType) throws BundleException {
        return GatewayActivator.disableGateway(getBundleContext(), gatewayType);
    }

    /**
     * Start gateway.
     *
     * @param gatewayType the name
     * @return the boolean
     */
    @POST
    @Path("/components/gateways/{type}/enable")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean enableGateway(@PathParam("type") final String gatewayType) throws BundleException {
        return GatewayActivator.enableGateway(getBundleContext(), gatewayType);
    }

    private <T extends EntityConfiguration> Collection<Map<String, String>> getDescription(final String componentName,
                                                                                                  final Class<T> entityType,
                                                                                           final EntityDescriptorProvider<T> descriptionProvider) {
        Collection<Map<String, String>> result;
        try {
            final ConfigurationEntityDescription<?> descriptor = descriptionProvider.getDescriptor(getBundleContext(), componentName, entityType);
            result = descriptor == null ? Collections.emptyList() : descriptor.stream()
                    .map(descriptor::getParameterDescriptor)
                    .map(ManagementService::stringifyDescription)
                    .collect(Collectors.toList());
        } catch (final UnsupportedOperationException exception) {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Gets gateway description.
     *
     * @param gatewayType the name
     * @return the entity description
     */
    @GET
    @Path("/components/gateways/{type}/description")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<Map<String, String>> getGatewayDescription(@PathParam("type") final String gatewayType) {
        return getDescription(gatewayType, GatewayConfiguration.class, GatewayClient::getConfigurationEntityDescriptor);
    }

    /**
     * Gets managed resource description.
     *
     * @param connectorType the name
     * @return the entity description
     */
    @GET
    @Path("/components/connectors/{type}/description")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<Map<String, String>> getConnectorDescription(@PathParam("type") final String connectorType) {
        return getDescription(connectorType, ManagedResourceConfiguration.class, ManagedResourceConnectorClient::getConfigurationEntityDescriptor);
    }

    /**
     * Gets resource attribute description.
     *
     * @param connectorType the name
     * @return the resource attribute description
     */
    @GET
    @Path("/components/connectors/{type}/attribute/description")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<Map<String, String>> getAttributeDescription(@PathParam("type") final String connectorType) {
        return getDescription(connectorType, AttributeConfiguration.class, ManagedResourceConnectorClient::getConfigurationEntityDescriptor);
    }

    /**
     * Gets resource event description.
     *
     * @param connectorType the name
     * @return the resource event description
     */
    @GET
    @Path("/components/connectors/{type}/event/description")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<Map<String, String>> getEventDescription(@PathParam("type") final String connectorType) {
        return getDescription(connectorType, EventConfiguration.class, ManagedResourceConnectorClient::getConfigurationEntityDescriptor);
    }

    /**
     * Gets resource operation description.
     *
     * @param connectorType the name
     * @return the resource operation description
     */
    @GET
    @Path("/components/connectors/{type}/operation/description")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<Map<String, String>> getOperationDescription(@PathParam("type") final String connectorType) {
        return getDescription(connectorType, OperationConfiguration.class, ManagedResourceConnectorClient::getConfigurationEntityDescriptor);
    }

    private static WebApplicationException configurationManagerIsNotAvailable(){
        return new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("ConfigurationManager is not available").build());
    }

    @Path("/configuration")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public AgentDataObject getConfiguration() {
        final BundleContext context = getBundleContext();
        return ServiceHolder.tryCreate(context, ConfigurationManager.class).map(manager -> {
            try {
                return manager.get().transformConfiguration(AgentDataObject::new);
            } catch (final IOException e) {
                throw new WebApplicationException(e);
            } finally {
                manager.release(context);
            }
        }).orElseThrow(ManagementService::configurationManagerIsNotAvailable);
    }

    @Path("/configuration/parameters")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getConfigurationParameters() {
        final BundleContext context = getBundleContext();
        return ServiceHolder.tryCreate(context, ConfigurationManager.class).map(manager -> {
            try {
                return manager.get().transformConfiguration(HashMap::new);
            } catch (final IOException e) {
                throw new WebApplicationException(e);
            } finally {
                manager.release(context);
            }
        }).orElseThrow(ManagementService::configurationManagerIsNotAvailable);
    }

    @Path("/configuration")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void setConfiguration(final AgentDataObject newConfig) {
        final BundleContext context = getBundleContext();
        ServiceHolder.tryCreate(context, ConfigurationManager.class).ifPresent(manager -> {
            try {
                manager.get().processConfiguration(existingConfig -> {
                    existingConfig.clear();
                    newConfig.exportTo(existingConfig);
                    return true;
                });
            } catch (final IOException e) {
                throw new WebApplicationException(e);
            } finally {
                manager.release(context);
            }
        });
    }

    @Path("/configuration/parameters")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void setConfigurationParameters(final Map<String, String> parameters) {
        final BundleContext context = getBundleContext();
        ServiceHolder.tryCreate(context, ConfigurationManager.class).ifPresent(manager -> {
            try {
                manager.get().processConfiguration(existingConfig -> {
                    existingConfig.load(parameters);
                    return true;
                });
            } catch (final IOException e) {
                throw new WebApplicationException(e);
            } finally {
                manager.release(context);
            }
        });
    }
}
