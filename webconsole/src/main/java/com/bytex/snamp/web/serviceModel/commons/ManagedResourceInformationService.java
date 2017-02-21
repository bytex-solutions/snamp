package com.bytex.snamp.web.serviceModel.commons;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.concurrent.AbstractConcurrentResourceAccessor;
import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.web.serviceModel.AbstractWebConsoleService;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.osgi.framework.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Optional;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.bytex.snamp.internal.Utils.isInstanceOf;

/**
 * Provides information about active managed resources and their attributes.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class ManagedResourceInformationService extends AbstractWebConsoleService implements ServiceListener, Constants {
    public static final String NAME = "managedResources";
    public static final String URL_CONTEXT = "/managedResources";

    //(componentType, resourceName)
    private final AbstractConcurrentResourceAccessor<Multimap<String, String>> resources;

    public ManagedResourceInformationService() {
        resources = new ConcurrentResourceAccessor<>(HashMultimap.create());
        ManagedResourceConnectorClient.addResourceListener(getBundleContext(), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        final BundleContext context = getBundleContext();
        for (final String resourceName : ManagedResourceConnectorClient.getResources(context)) {
            final ManagedResourceConnectorClient client = ManagedResourceConnectorClient.tryCreate(context, resourceName);
            if (client != null)
                try {
                    connectorChanged(client, ServiceEvent.REGISTERED);
                } finally {
                    client.release(context);
                }
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{instanceName}/attributes")
    public AttributeInformation[] getInstanceAttributes(@PathParam("instanceName") final String instanceName) {
        final BundleContext context = getBundleContext();
        final ManagedResourceConnectorClient client = ManagedResourceConnectorClient.tryCreate(context, instanceName);
        if (client == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        else
            try {
                return ArrayUtils.transform(client.getMBeanInfo().getAttributes(), AttributeInformation.class, AttributeInformation::new);
            } finally {
                client.release(getBundleContext());
            }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/components/{componentName}/attributes")
    public AttributeInformation[] getComponentAttributes(@PathParam("componentName") final String componentName) {
        final ServiceHolder<ConfigurationManager> configurationManager = ServiceHolder.tryCreate(getBundleContext(), ConfigurationManager.class);
        if (configurationManager != null)
            try {
                final Optional<? extends ManagedResourceGroupConfiguration> group = configurationManager.get().transformConfiguration(config -> config.getEntities(ManagedResourceGroupConfiguration.class).getIfPresent(componentName));
                if(group.isPresent())
                    return group.get().getFeatures(AttributeConfiguration.class)
                            .entrySet()
                            .stream()
                            .map(entry -> new AttributeInformation(entry.getKey(), entry.getValue()))
                            .toArray(AttributeInformation[]::new);
                else
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(String.format("Component/group %s not found", componentName)).build());
            } catch (final IOException e) {
                throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
            }
            finally {
                configurationManager.release(getBundleContext());
            }
        else
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("ConfigurationManager is not available").build());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/components")
    public String[] getComponents() {
        return resources.read(resources -> resources.keySet().stream().toArray(String[]::new));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getInstances(@QueryParam("component") @DefaultValue("") final String componentName) {
        return Strings.isNullOrEmpty(componentName) ?
                resources.read(resources -> resources.values().stream().toArray(String[]::new)) :
                resources.read(resources -> resources.get(componentName).stream().toArray(String[]::new));
    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    private void connectorChanged(final ManagedResourceConnectorClient client, final int type) {
        switch (type) {
            case ServiceEvent.REGISTERED:
                resources.write(resources -> resources.put(client.getGroupName(), client.getManagedResourceName()));
                return;
            case ServiceEvent.UNREGISTERING:
            case ServiceEvent.MODIFIED_ENDMATCH:
                resources.write(resources -> resources.remove(client.getGroupName(), client.getManagedResourceName()));
        }
    }

    @Override
    public void serviceChanged(final ServiceEvent event) {
        if (isInstanceOf(event.getServiceReference(), ManagedResourceConnector.class)) {
            @SuppressWarnings("unchecked")
            final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(getBundleContext(), (ServiceReference<ManagedResourceConnector>) event.getServiceReference());
            try {
                connectorChanged(client, event.getType());
            } finally {
                client.release(getBundleContext());
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (isInitialized())
            getBundleContext().removeServiceListener(this);
        super.close();
    }
}
