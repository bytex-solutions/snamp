package com.bytex.snamp.web.serviceModel.commons;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.web.serviceModel.AbstractWebConsoleService;
import com.bytex.snamp.web.serviceModel.RESTController;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

/**
 * Provides information about active managed resources and their attributes.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class ManagedResourceInformationService extends AbstractWebConsoleService implements RESTController {
    private static final String URL_CONTEXT = "/managedResources";
    private final ResourceGroupTracker tracker;

    public ManagedResourceInformationService() {
        tracker = new ResourceGroupTracker();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        try {
            tracker.startTracking();
        } catch (final Exception e) {
            getLogger().log(Level.SEVERE, "Unable to start tracking resources", e);
        }
    }

    @Override
    public String getUrlContext() {
        return URL_CONTEXT;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{instanceName}/attributes")
    public AttributeInformation[] getInstanceAttributes(@PathParam("instanceName") final String instanceName) {
        try (final ManagedResourceConnectorClient client = ManagedResourceConnectorClient.tryCreate(getBundleContext(), instanceName)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND))) {
            return ArrayUtils.transform(client.getMBeanInfo().getAttributes(), AttributeInformation.class, AttributeInformation::new);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/components/{componentName}/attributes")
    public AttributeInformation[] getGroupAttributes(@PathParam("componentName") final String componentName) {
        return ServiceHolder.tryCreate(getBundleContext(), ConfigurationManager.class)
                .map(configurationManager -> {
                    try {
                        final Optional<? extends ManagedResourceGroupConfiguration> group = configurationManager.get().transformConfiguration(config -> config.getResourceGroups().getIfPresent(componentName));
                        if (group.isPresent())
                            return group.get().getAttributes()
                                    .entrySet()
                                    .stream()
                                    .map(entry -> new AttributeInformation(entry.getKey(), entry.getValue()))
                                    .toArray(AttributeInformation[]::new);
                        else
                            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("Component/group %s not found", componentName)).build());
                    } catch (final IOException e) {
                        throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
                    } finally {
                        configurationManager.release(getBundleContext());
                    }
                })
                .orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("ConfigurationManager is not available").build()));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/components")
    public Set<String> getGroups() {
        return tracker.getGroups();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getInstances(@QueryParam("component") @DefaultValue("") final String groupName) {
        return tracker.getResources(groupName);
    }

    @Override
    public void close() throws Exception {
        Utils.closeAll(tracker, super::close);
    }
}
