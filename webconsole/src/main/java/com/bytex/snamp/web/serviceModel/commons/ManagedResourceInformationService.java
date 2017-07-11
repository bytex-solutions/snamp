package com.bytex.snamp.web.serviceModel.commons;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
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
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    private static AttributeInformation[] getCommonAttributes(final Collection<ManagedResourceConfiguration> resources) {
        //wrapper that overrides equality for AttributeInformation and takes into account only type and name
        final class AttributeInformationWrapper implements Supplier<AttributeInformation> {
            private final AttributeInformation information;

            private AttributeInformationWrapper(final String attributeName, final AttributeConfiguration configuration) {
                this.information = new AttributeInformation(attributeName, configuration);
            }

            private AttributeInformationWrapper(final Map.Entry<String, ? extends AttributeConfiguration> entry){
                this(entry.getKey(), entry.getValue());
            }

            @Override
            public AttributeInformation get() {
                return information;
            }

            @Override
            public int hashCode() {
                return Objects.hash(information.getType(), information.getName());
            }

            private boolean equals(final AttributeInformation other) {
                return Objects.equals(information.getType(), other.getType()) &&
                        Objects.equals(information.getName(), other.getName());
            }

            private boolean equals(final AttributeInformationWrapper other) {
                return equals(other.get());
            }

            @Override
            public boolean equals(final Object other) {
                return other instanceof AttributeInformationWrapper && equals((AttributeInformationWrapper) other);
            }
        }
        final Multiset<AttributeInformationWrapper> attributes =
                resources.stream()
                        .map(ManagedResourceConfiguration::getAttributes)
                        .flatMap(attrs -> attrs.entrySet().stream().map(AttributeInformationWrapper::new))
                        .collect(HashMultiset::create, Multiset::add, Multiset::addAll);

        return attributes
                .elementSet()
                .stream()
                .filter(wrapper -> attributes.count(wrapper) == resources.size())
                .map(AttributeInformationWrapper::get)
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
            //try to extract attributes from resources with the same group name
            return configurationManager.get().transformConfiguration(config -> {
                final Collection<ManagedResourceConfiguration> resources = config.getResources().values()
                        .stream()
                        .filter(resource -> Objects.equals(resource.getGroupName(), groupName))
                        .collect(Collectors.toList());
                if (resources.isEmpty())
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                else
                    return getCommonAttributes(resources);
            });
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
