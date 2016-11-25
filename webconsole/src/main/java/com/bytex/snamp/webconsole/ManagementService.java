package com.bytex.snamp.webconsole;

import com.bytex.snamp.management.ManagementUtils;
import com.bytex.snamp.management.rest.SnampRestManagerImpl;
import com.google.common.collect.ImmutableMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

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
                        .put("type", INTERNAL_COMPONENT_TYPE_NAME)
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
                        .put("type", INTERNAL_COMPONENT_TYPE_NAME)
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
}
