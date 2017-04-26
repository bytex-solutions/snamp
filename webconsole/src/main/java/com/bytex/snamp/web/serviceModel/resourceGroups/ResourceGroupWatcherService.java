package com.bytex.snamp.web.serviceModel.resourceGroups;

import com.bytex.snamp.Convert;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.supervision.GroupCompositionChanged;
import com.bytex.snamp.supervision.SupervisionEvent;
import com.bytex.snamp.supervision.SupervisorClient;
import com.bytex.snamp.supervision.health.HealthStatusChangedEvent;
import com.bytex.snamp.web.serviceModel.AbstractWebConsoleService;
import com.bytex.snamp.web.serviceModel.RESTController;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.annotation.Nonnull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Set;
import java.util.logging.Level;

/**
 * Represents notification service.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
@Path("/")
public final class ResourceGroupWatcherService extends AbstractWebConsoleService implements RESTController, EventAcceptor {
    private static final String URL_CONTEXT = "/resource-group-watcher";

    @JsonTypeName("healthStatusChanged")
    public final class GroupStatusChangedMessage extends WebConsoleServiceMessage{
        private static final long serialVersionUID = -9201166624972276258L;
        private final HealthStatus previousStatus;
        private final HealthStatus newStatus;
        private final String groupName;

        private GroupStatusChangedMessage(final HealthStatusChangedEvent event, final String groupName) {
            this.previousStatus = event.getPreviousStatus();
            this.newStatus = event.getNewStatus();
            this.groupName = groupName;
        }

        @JsonProperty("groupName")
        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public String getGroupName(){
            return groupName;
        }

        @JsonProperty("previousStatus")
        @JsonSerialize(using = HealthStatusSerializer.class)
        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public HealthStatus getPreviousStatus(){
            return previousStatus;
        }

        @JsonProperty("newStatus")
        @JsonSerialize(using = HealthStatusSerializer.class)
        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public HealthStatus getNewStatus(){
            return newStatus;
        }
    }

    @JsonTypeName("groupCompositionChanged")
    public final class GroupCompositionChangedMessage extends WebConsoleServiceMessage{
        private static final long serialVersionUID = 469376947661340798L;
        private final GroupCompositionChanged event;

        private GroupCompositionChangedMessage(@Nonnull final GroupCompositionChanged event){
            this.event = event;
        }

        @JsonProperty("modifier")
        @JsonSerialize(using = ModifierSerializer.class)
        public GroupCompositionChanged.Modifier getModifier(){
            return event.getModifier();
        }

        @JsonProperty("resourceName")
        public String getResourceName(){
            return event.getResourceName();
        }

        @JsonProperty("groupName")
        public String getGroupName(){
            return event.getGroupName();
        }
    }

    private final ResourceGroupEventHub hub;

    public ResourceGroupWatcherService(){
        hub = new ResourceGroupEventHub();
    }

    @Override
    protected void initialize() {
        try {
            hub.startTracking(this);
        } catch (final Exception e) {
            getLogger().log(Level.SEVERE, "Unable to start tracking health status", e);
        }
    }

    @Override
    public String getUrlContext() {
        return URL_CONTEXT;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/groups")
    public Set<String> getGroups() {
        return SupervisorClient.filterBuilder().getGroups(getBundleContext());
    }

    @GET
    @Path("/groups/status")
    @Produces(MediaType.APPLICATION_JSON)
    public HealthStatusMap getStatus() {
        final HealthStatusMap result = new HealthStatusMap();
        for (final String groupName : getGroups())
            SupervisorClient.tryCreate(getBundleContext(), groupName).ifPresent(client -> {
                result.putStatus(groupName, client);
                client.close();
            });
        return result;
    }



    @Override
    public void handle(@Nonnull final SupervisionEvent event) {
        Convert.toType(event, GroupCompositionChanged.class)
                .map(GroupCompositionChangedMessage::new)
                .ifPresent(this::sendBroadcastMessage);
    }

    @Override
    public void statusChanged(@Nonnull final HealthStatusChangedEvent event, final Object handback) {
        final String groupName = Convert.toType(handback, String.class).orElseThrow(AssertionError::new);
        sendBroadcastMessage(new GroupStatusChangedMessage(event, groupName));
    }

    @Override
    public void close() throws Exception {
        Utils.closeAll(hub, super::close);
    }
}
