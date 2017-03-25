package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.SupervisorConfiguration;
import com.bytex.snamp.management.http.model.SupervisorDataObject;
import com.bytex.snamp.management.http.model.ScriptletDataObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Optional;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/configuration/watcher")
public final class ResourceGroupWatcherConfigurationService extends AbstractEntityConfigurationService<SupervisorConfiguration, SupervisorDataObject> {
    ResourceGroupWatcherConfigurationService(){
        super(SupervisorConfiguration.class);
    }

    @Override
    protected SupervisorDataObject toDataTransferObject(final SupervisorConfiguration entity) {
        return new SupervisorDataObject(entity);
    }

    @GET
    @Path("/{groupName}/trigger")
    @Produces(MediaType.APPLICATION_JSON)
    public ScriptletDataObject getTrigger(@PathParam("groupName") final String groupName){
        return getConfigurationByName(groupName, config -> new ScriptletDataObject(config.getHealthCheckConfig().getTrigger()));
    }

    @PUT
    @Path("/{groupName}/trigger")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setTrigger(@PathParam("groupName") final String groupName,
                           final ScriptletDataObject trigger,
                           @Context final SecurityContext context) {
        setConfigurationByName(groupName, config -> trigger.exportTo(config.getHealthCheckConfig().getTrigger()), context);
    }

    @Path("/{groupName}/attributeChecker/{attributeName}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ScriptletDataObject getAttributeChecker(@PathParam("groupName") final String groupName, @PathParam("attributeName") final String attributeName){
        final Optional<ScriptletDataObject> checker =  getConfigurationByName(groupName,
                config -> Optional.ofNullable(config.getHealthCheckConfig().getAttributeCheckers().get(attributeName)).map(ScriptletDataObject::new));
        if(checker.isPresent())
            return checker.get();
        else
            throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @Path("/{groupName}/attributeChecker/{attributeName}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void setAttributeChecker(@PathParam("groupName") final String groupName,
                                    @PathParam("attributeName") final String attributeName,
                                    final ScriptletDataObject checker,
                                    @Context final SecurityContext context){
        setConfigurationByName(groupName, config -> checker.exportTo(config.getHealthCheckConfig().getAttributeCheckers().getOrAdd(attributeName)), context);
    }
}
