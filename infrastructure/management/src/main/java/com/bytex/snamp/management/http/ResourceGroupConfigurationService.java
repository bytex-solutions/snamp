package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.EntityMapResolver;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;
import com.bytex.snamp.management.http.model.ResourceGroupDataObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides API for SNAMP resource group.
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@Path("/configuration/resourceGroup")
public final class ResourceGroupConfigurationService extends TemplateConfigurationService<ManagedResourceGroupConfiguration, ResourceGroupDataObject> {
    ResourceGroupConfigurationService() {
        super(EntityMapResolver.GROUPS);
    }

    @Override
    protected ResourceGroupDataObject toDataTransferObject(final ManagedResourceGroupConfiguration entity) {
        return new ResourceGroupDataObject(entity);
    }

    /**
     * Gets resource group names.
     *
     * @return the resource group names
     */
    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Set<String> getResourceGroupNames() {
        return readOnlyActions(getBundleContext(), config -> entityMapResolver.apply(config)
                .entrySet()
                .stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet()));
    }
}
