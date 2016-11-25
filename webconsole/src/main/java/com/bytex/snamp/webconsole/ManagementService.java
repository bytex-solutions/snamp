package com.bytex.snamp.webconsole;

import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.management.ManagementUtils;
import com.bytex.snamp.management.rest.SnampRestManagerImpl;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * ManagedResourceConfigurationDTO
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@Path("/management")
public class ManagementService extends BaseRestConfigurationService {

    private static final String INTERNAL_COMPONENT_TYPE_NAME = "Internal component";
    private static final String GATEWAY_COMPONENT_TYPE_NAME = "Gateway";
    private static final String RESOURCE_COMPONENT_TYPE_NAME = "Managed resource";

    private static Collection<Map<String, String>> getInternalComponents() {
        return new SnampRestManagerImpl().getInstalledComponents()
                .stream()
                .filter(entry -> entry.getName(Locale.getDefault()) != null)
                .map(entry -> ImmutableMap.<String,String>builder()
                        .put("name", entry.getName(Locale.getDefault()))
                        .put("description", entry.toString(Locale.getDefault()))
                        .put("state", ManagementUtils.getStateString(entry))
                        .put("version", entry.getVersion().toString())
                        .put("type", INTERNAL_COMPONENT_TYPE_NAME)
                        .build())
                .collect(Collectors.toList());
    }

    private static Collection<Map<String, String>> getGatewayComponents() {
        return new SnampRestManagerImpl().getInstalledGateways()
                .stream()
                .filter(entry -> entry.getName(Locale.getDefault()) != null)
                .map(entry -> ImmutableMap.<String,String>builder()
                        .put("name", entry.getName(Locale.getDefault()))
                        .put("description", entry.toString(Locale.getDefault()))
                        .put("state", ManagementUtils.getStateString(entry))
                        .put("version", entry.getVersion().toString())
                        .put("type", GATEWAY_COMPONENT_TYPE_NAME)
                        .build())
                .collect(Collectors.toList());
    }

    private static Collection<Map<String, String>> getResourceComponents() {
        return new SnampRestManagerImpl().getInstalledResourceConnectors()
                .stream()
                .filter(entry -> entry.getName(Locale.getDefault()) != null)
                .map(entry -> ImmutableMap.<String,String>builder()
                        .put("name", entry.getName(Locale.getDefault()))
                        .put("description", entry.toString(Locale.getDefault()))
                        .put("state", ManagementUtils.getStateString(entry))
                        .put("version", entry.getVersion().toString())
                        .put("type", RESOURCE_COMPONENT_TYPE_NAME)
                        .build())
                .collect(Collectors.toList());
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
        final Collection<Map<String,String>> collection = getInternalComponents();
        collection.addAll(getGatewayComponents());
        collection.addAll(getResourceComponents());
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
    public Collection getInstalledResources() {
        return getResourceComponents();
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
    public Collection getInstalledGateways() {
        return getGatewayComponents();
    }

    /**
     * Restart all the system.
     */
    @GET
    @Path("/restart")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public void restartAllTheSystem() {
        try {
            SnampRestManagerImpl.restart(getBundleContextOfObject(this));
        } catch (final BundleException e) {
            throw new WebApplicationException(e);
        }
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
    public Boolean disableConnector(@PathParam("name") final String name)  {
        try {
            return ManagedResourceActivator.disableConnector(getBundleContextOfObject(this), name);
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
    public Boolean enableConnector(@PathParam("name") final String name)  {
        try {
            return ManagedResourceActivator.enableConnector(getBundleContextOfObject(this), name);
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
    public Boolean disableGateway(@PathParam("name") final String name)  {
        try {
            return GatewayActivator.disableGateway(getBundleContextOfObject(this), name);
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
    public Boolean enableGateway(@PathParam("name") final String name)  {
        try {
            return GatewayActivator.enableGateway(getBundleContextOfObject(this), name);
        } catch (final BundleException e) {
            throw new WebApplicationException(e);
        }
    }
}
