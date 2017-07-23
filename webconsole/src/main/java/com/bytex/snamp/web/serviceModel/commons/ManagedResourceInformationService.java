package com.bytex.snamp.web.serviceModel.commons;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.web.serviceModel.AbstractWebConsoleService;
import com.bytex.snamp.web.serviceModel.RESTController;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.osgi.framework.BundleContext;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

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

    private static AttributeInformation[] getCommonAttributes(final BundleContext context, final Set<String> resources) {
        if (resources.isEmpty())
            return ArrayUtils.emptyArray(AttributeInformation[].class);
        final Multiset<AttributeInformation> attributes = resources.stream()
                .map(resourceName -> ManagedResourceConnectorClient.tryCreate(context, resourceName))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(client -> {
                    try {
                        return Arrays.stream(client.getMBeanInfo().getAttributes()).map(AttributeInformation::new);
                    } finally {
                        client.close();
                    }
                })
                .collect(HashMultiset::create, Multiset::add, Multiset::addAll);

        return attributes
                .elementSet()
                .stream()
                .filter(wrapper -> attributes.count(wrapper) == resources.size())
                .toArray(AttributeInformation[]::new);
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
        } catch (final IOException e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            configurationManager.release(context);
        }
        final Set<String> resources = ManagedResourceConnectorClient.selector().setGroupName(groupName).getResources(context);
        //try to extract attributes from resources with the same group name
        if (resources.isEmpty())
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        else
            return getCommonAttributes(context, resources);
    }

    private static WebApplicationException noConfigurationManager(){
        return new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("ConfigurationManager is not available").build());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resources/{resourceName}/attributes")
    public AttributeInformation[] getResourceAttributes(@PathParam("resourceName") final String resourceName) {
        try (final ManagedResourceConnectorClient client = ManagedResourceConnectorClient.tryCreate(getBundleContext(), resourceName)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND))) {
            return Arrays.stream(client.getMBeanInfo().getAttributes()).map(AttributeInformation::new).toArray(AttributeInformation[]::new);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/resources/attributes")
    public AttributeInformation[] getAttributes(final Set<String> resources) {
        return getCommonAttributes(getBundleContext(), resources);
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getGroups() {
        //list of groups can be extracted from already instantiated resources. No need to return groups without resources
        return ManagedResourceConnectorClient.selector().getGroups(getBundleContext());
    }

    @GET
    @Path("/resources")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getResources() {
        return ManagedResourceConnectorClient.selector().getResources(getBundleContext());
    }

    @GET
    @Path("{groupName}/resources")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getResources(@PathParam("groupName")final String groupName) {
        return ManagedResourceConnectorClient.selector().setGroupName(groupName).getResources(getBundleContext());
    }
}
