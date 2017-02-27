package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.ManagedResourceGroupWatcherConfiguration;
import com.bytex.snamp.management.http.model.ResourceGroupWatcherDataObject;
import com.bytex.snamp.management.http.model.ScriptletDataObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/configuration/watchers")
public final class ResourceGroupWatcherConfigurationService extends AbstractEntityConfigurationService<ManagedResourceGroupWatcherConfiguration, ResourceGroupWatcherDataObject> {
    ResourceGroupWatcherConfigurationService(){
        super(ManagedResourceGroupWatcherConfiguration.class);
    }

    @Override
    protected ResourceGroupWatcherDataObject toDataTransferObject(final ManagedResourceGroupWatcherConfiguration entity) {
        return new ResourceGroupWatcherDataObject(entity);
    }

    @GET
    @Path("/{groupName}/trigger")
    @Produces(MediaType.APPLICATION_JSON)
    public ScriptletDataObject getTrigger(@PathParam("groupName") final String groupName){
        return getConfigurationByName(groupName, config -> new ScriptletDataObject(config.getTrigger()));
    }

    @PUT
    @Path("/{groupName}/trigger")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setTrigger(@PathParam("groupName") final String groupName, final ScriptletDataObject trigger) {
        setConfigurationByName(groupName, config -> trigger.exportTo(config.getTrigger()));
    }

    @Path("/{groupName}/attributeCheckers/{attributeName}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ScriptletDataObject getAttributeChecker(@PathParam("groupName") final String groupName, @PathParam("attributeName") final String attributeName){
        final Optional<ScriptletDataObject> checker =  getConfigurationByName(groupName,
                config -> Optional.ofNullable(config.getAttributeCheckers().get(attributeName)).map(ScriptletDataObject::new));
        if(checker.isPresent())
            return checker.get();
        else
            throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @Path("/{groupName}/attributeCheckers/{attributeName}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void setAttributeChecker(@PathParam("groupName") final String groupName, @PathParam("attributeName") final String attributeName, final ScriptletDataObject checker){
        setConfigurationByName(groupName, config -> checker.exportTo(config.getAttributeCheckers().getOrAdd(attributeName)));
    }
}
