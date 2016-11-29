package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;
import com.bytex.snamp.management.http.model.ResourceGroupDataObject;

import javax.ws.rs.Path;

/**
 * Provides API for SNAMP resource group.
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@Path("/resourceGroup")
public final class ResourceGroupConfigurationService extends TemplateConfigurationService<ManagedResourceGroupConfiguration, ResourceGroupDataObject> {
    public ResourceGroupConfigurationService(){
        super(ManagedResourceGroupConfiguration.class);
    }


    @Override
    protected ResourceGroupDataObject toDataTransferObject(final ManagedResourceGroupConfiguration entity) {
        return new ResourceGroupDataObject(entity);
    }
}
