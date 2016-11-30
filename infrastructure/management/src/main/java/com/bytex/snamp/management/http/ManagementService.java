package com.bytex.snamp.management.http;

import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.core.AbstractSnampManager;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.management.ManagementUtils;
import com.bytex.snamp.management.SnampManagerImpl;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
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
@Path("/management")
public final class ManagementService extends AbstractManagementService {
    private static final String INTERNAL_COMPONENT_TYPE_NAME = "Internal component";
    private static final String GATEWAY_COMPONENT_TYPE_NAME = "Gateway";
    private static final String RESOURCE_COMPONENT_TYPE_NAME = "Managed resource";
    private final AbstractSnampManager manager = new SnampManagerImpl();

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
    @Path("/resources")
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
    @Path("/gateways")
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
                        .put("type", entry.getType())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Restart all the system.
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
    @Path("/resources/{name}/disable")
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
    @Path("/resources/{name}/enable")
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
    @Path("/gateways/{name}/disable")
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
    @Path("/gateways/{name}/enable")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public boolean enableGateway(@PathParam("name") final String name)  {
        try {
            return GatewayActivator.enableGateway(getBundleContext(), name);
        } catch (final BundleException e) {
            throw new WebApplicationException(e);
        }
    }
}
