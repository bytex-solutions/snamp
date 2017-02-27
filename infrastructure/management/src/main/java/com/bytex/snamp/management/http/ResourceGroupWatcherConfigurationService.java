package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.ManagedResourceGroupWatcherConfiguration;
import com.bytex.snamp.management.http.model.ResourceGroupWatcherDataObject;

import javax.ws.rs.Path;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/configuration/watchers")
public final class ResourceGroupWatcherConfigurationService extends AbstractEntityConfigurationService<ManagedResourceGroupWatcherConfiguration, ResourceGroupWatcherDataObject> {
    public ResourceGroupWatcherConfigurationService(){
        super(ManagedResourceGroupWatcherConfiguration.class);
    }

    @Override
    protected ResourceGroupWatcherDataObject toDataTransferObject(final ManagedResourceGroupWatcherConfiguration entity) {
        return new ResourceGroupWatcherDataObject(entity);
    }
}
