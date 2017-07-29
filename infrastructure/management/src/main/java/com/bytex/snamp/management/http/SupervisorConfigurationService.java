package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.EntityMapResolver;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import com.bytex.snamp.management.http.model.ScriptletDataObject;
import com.bytex.snamp.management.http.model.SupervisorDataObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@Path("/configuration/supervisor")
public final class SupervisorConfigurationService extends AbstractEntityConfigurationService<SupervisorConfiguration, SupervisorDataObject> {
    SupervisorConfigurationService() {
        super(EntityMapResolver.SUPERVISORS);
    }

    @Override
    protected SupervisorDataObject toDataTransferObject(final SupervisorConfiguration entity) {
        return new SupervisorDataObject(entity);
    }

    @GET
    @Path("/{groupName}/discovery/connectionStringTemplate")
    @Produces(MediaType.APPLICATION_JSON)
    public String getConnectionStringTemplate(@PathParam("groupName") final String groupName){
        return getConfigurationByName(groupName, config -> config.getDiscoveryConfig().getConnectionStringTemplate());
    }

    @PUT
    @Path("/{groupName}/discovery/connectionStringTemplate")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setConnectionStringTemplate(@PathParam("groupName") final String groupName,
                                            final String template) {
        setConfigurationByName(groupName, config -> config.getDiscoveryConfig().setConnectionStringTemplate(template));
    }

    @GET
    @Path("/{groupName}/healthCheck/trigger")
    @Produces(MediaType.APPLICATION_JSON)
    public ScriptletDataObject getTrigger(@PathParam("groupName") final String groupName){
        return getConfigurationByName(groupName, config -> new ScriptletDataObject(config.getHealthCheckConfig().getTrigger()));
    }

    @PUT
    @Path("/{groupName}/healthCheck/trigger")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setTrigger(@PathParam("groupName") final String groupName,
                           final ScriptletDataObject trigger) {
        setConfigurationByName(groupName, config -> trigger.exportTo(config.getHealthCheckConfig().getTrigger()));
    }

    @Path("/{groupName}/healthCheck/attributeChecker/{attributeName}")
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

    @Path("/{groupName}/healthCheck/attributeChecker/{attributeName}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void setAttributeChecker(@PathParam("groupName") final String groupName,
                                    @PathParam("attributeName") final String attributeName,
                                    final ScriptletDataObject checker){
        setConfigurationByName(groupName, config -> checker.exportTo(config.getHealthCheckConfig().getAttributeCheckers().getOrAdd(attributeName)));
    }
}
