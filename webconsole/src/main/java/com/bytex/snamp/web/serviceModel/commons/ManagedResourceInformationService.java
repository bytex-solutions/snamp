package com.bytex.snamp.web.serviceModel.commons;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.web.serviceModel.AbstractWebConsoleService;
import com.bytex.snamp.web.serviceModel.RESTController;
import com.google.common.collect.ImmutableSet;
import org.osgi.framework.BundleContext;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Provides information about active managed resources and their attributes.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class ManagedResourceInformationService extends AbstractWebConsoleService implements RESTController {
    private static final String URL_CONTEXT = "/groups";

    public ManagedResourceInformationService() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
    }

    @Override
    public String getUrlContext() {
        return URL_CONTEXT;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceName}/attributes")
    public AttributeInformation[] getResourceAttributes(@PathParam("resourceName") final String resourceName) {
        try (final ManagedResourceConnectorClient client = ManagedResourceConnectorClient.tryCreate(getBundleContext(), resourceName)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND))) {
            return ArrayUtils.transform(client.getMBeanInfo().getAttributes(), AttributeInformation.class, AttributeInformation::new);
        }
    }

    private static AttributeInformation[] getGroupAttributes(final BundleContext context,
                                                             final String groupName,
                                                             final ServiceHolder<ConfigurationManager> configurationManager) {
        try {
            final Optional<? extends ManagedResourceGroupConfiguration> group = configurationManager.get().transformConfiguration(config -> config.getResourceGroups().getIfPresent(groupName));
            if (group.isPresent())
                return group.get().getAttributes()
                        .entrySet()
                        .stream()
                        .map(entry -> new AttributeInformation(entry.getKey(), entry.getValue()))
                        .toArray(AttributeInformation[]::new);
            else
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("Group %s not found", groupName)).build());
        } catch (final IOException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            configurationManager.release(context);
        }
    }

    private static WebApplicationException noConfigurationManager(){
        return new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("ConfigurationManager is not available").build());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{groupName}/attributes")
    public AttributeInformation[] getGroupAttributes(@PathParam("groupName") final String groupName) {
        final BundleContext context = getBundleContext();
        return ServiceHolder.tryCreate(context, ConfigurationManager.class)
                .map(configurationManager -> getGroupAttributes(context, groupName, configurationManager))
                .orElseThrow(ManagedResourceInformationService::noConfigurationManager);
    }

    private static Set<String> getGroups(final BundleContext context, final ServiceHolder<ConfigurationManager> configurationManager){
        try{
            return configurationManager.get().transformConfiguration(config -> ImmutableSet.copyOf(config.getResourceGroups().keySet()));
        } catch (final IOException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            configurationManager.release(context);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getGroups() {
        final BundleContext context = getBundleContext();
        return ServiceHolder.tryCreate(context, ConfigurationManager.class)
                .map(holder -> getGroups(context, holder))
                .orElseGet(ImmutableSet::of);
    }

    @GET
    @Path("/resources")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getResources(@QueryParam("groupName") @DefaultValue("") final String groupName) {
        return isNullOrEmpty(groupName) ?
                ManagedResourceConnectorClient.filterBuilder().getResources(getBundleContext()) :
                ManagedResourceConnectorClient.filterBuilder().setGroupName(groupName).getResources(getBundleContext());
    }
}
