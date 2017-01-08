package com.bytex.snamp.web.serviceModel.managedResources;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.concurrent.AbstractConcurrentResourceAccessor;
import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.web.serviceModel.AbstractWebConsoleService;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.osgi.framework.*;

import javax.management.InstanceNotFoundException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Objects;

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
    public static final String NAME = "componentInfo";
    public static final String CONTEXT = "/components";

    //(componentType, resourceName)
    private final AbstractConcurrentResourceAccessor<Multimap<String, String>> resources;

    public ManagedResourceInformationService() {
        resources = new ConcurrentResourceAccessor<>(HashMultimap.create());
        ManagedResourceConnectorClient.addResourceListener(getBundleContext(), this);
    }

    /**
     * Initializes this service.
     */
    public void init() {
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
    @Path("/attributes")
    public AttributeInformation[] getAttributes(@QueryParam("instanceName") final String instanceName) {
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
    public String[] getComponents() {
        return resources.read(resources -> resources.keySet().stream().toArray(String[]::new));
    }

    @GET
    @Path("/instances")
    public String[] getInstances(@QueryParam("componentName") final String componentName) {
        return resources.read(resources -> resources.get(componentName).stream().toArray(String[]::new));
    }

    private static String getComponentName(final ManagedResourceConnectorClient client){
        String componentName = Objects.toString(client.getProperty(ManagedResourceConfiguration.GROUP_NAME_PROPERTY), "");
        if(isNullOrEmpty(componentName))
            componentName = client.getConnectorType();
        return componentName;
    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    private void connectorChanged(final ManagedResourceConnectorClient client, final int type) {
        switch (type) {
            case ServiceEvent.REGISTERED:
                resources.write(resources -> resources.put(getComponentName(client), client.getManagedResourceName()));
                return;
            case ServiceEvent.UNREGISTERING:
            case ServiceEvent.MODIFIED_ENDMATCH:
                resources.write(resources -> resources.remove(getComponentName(client), client.getManagedResourceName()));
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
        getBundleContext().removeServiceListener(this);
        super.close();
    }
}
