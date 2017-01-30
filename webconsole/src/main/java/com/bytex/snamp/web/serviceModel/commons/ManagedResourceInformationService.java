package com.bytex.snamp.web.serviceModel.commons;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.concurrent.AbstractConcurrentResourceAccessor;
import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;
import com.bytex.snamp.connector.ClusteredResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.web.serviceModel.AbstractWebConsoleService;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.osgi.framework.*;

import javax.management.InstanceNotFoundException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.bytex.snamp.internal.Utils.isInstanceOf;
import static com.google.common.base.Strings.isNullOrEmpty;

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
        for (final ServiceReference<ManagedResourceConnector> connectorRef : ManagedResourceConnectorClient.getConnectors(getBundleContext()).values()) {
            final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(getBundleContext(), connectorRef);
            try {
                connectorChanged(client, ServiceEvent.REGISTERED);
            } finally {
                client.release(getBundleContext());
            }
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{instanceName}/attributes")
    public AttributeInformation[] getAttributes(@PathParam("instanceName") final String instanceName) {
        ManagedResourceConnectorClient client = null;
        try {
            client = new ManagedResourceConnectorClient(getBundleContext(), instanceName);
            return ArrayUtils.transform(client.getMBeanInfo().getAttributes(), AttributeInformation.class, AttributeInformation::new);
        } catch (final InstanceNotFoundException e) {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        } finally {
            if (client != null)
                client.release(getBundleContext());
        }
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

    private static String getComponentName(final ManagedResourceConnectorClient client) {
        final ClusteredResourceConnector clusteredResource = client.queryObject(ClusteredResourceConnector.class);
        String componentName;
        if (clusteredResource == null)
            componentName = client.getConfiguration().getGroupName();
        else
            componentName = clusteredResource.getComponentName();
        return isNullOrEmpty(componentName) ? client.getManagedResourceName() : componentName;
    }

    private static String getInstanceName(final ManagedResourceConnectorClient client) {
        final ClusteredResourceConnector clusteredResource = client.queryObject(ClusteredResourceConnector.class);
        return clusteredResource == null ? client.getManagedResourceName() : clusteredResource.getInstanceName();
    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    private void connectorChanged(final ManagedResourceConnectorClient client, final int type) {
        switch (type) {
            case ServiceEvent.REGISTERED:
                resources.write(resources -> resources.put(getComponentName(client), getInstanceName(client)));
                return;
            case ServiceEvent.UNREGISTERING:
            case ServiceEvent.MODIFIED_ENDMATCH:
                resources.write(resources -> resources.remove(getComponentName(client), getInstanceName(client)));
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