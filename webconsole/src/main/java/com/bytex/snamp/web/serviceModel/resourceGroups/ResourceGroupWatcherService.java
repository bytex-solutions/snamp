package com.bytex.snamp.web.serviceModel.resourceGroups;

import com.bytex.snamp.Convert;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.json.InstantSerializer;
import com.bytex.snamp.supervision.GroupCompositionChanged;
import com.bytex.snamp.supervision.SupervisionEvent;
import com.bytex.snamp.supervision.SupervisionEventListener;
import com.bytex.snamp.supervision.SupervisorClient;
import com.bytex.snamp.supervision.health.HealthStatusChangedEvent;
import com.bytex.snamp.supervision.health.HealthStatusProvider;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;
import com.bytex.snamp.web.serviceModel.AbstractWebConsoleService;
import com.bytex.snamp.web.serviceModel.RESTController;
import com.bytex.snamp.web.serviceModel.WebMessage;
import com.bytex.snamp.web.serviceModel.charts.HealthStatusSerializer;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.annotation.Nonnull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Represents notification service.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
@Path("/")
public final class ResourceGroupWatcherService extends AbstractWebConsoleService implements RESTController, SupervisionEventListener {
    private static final String URL_CONTEXT = "/resource-group-watcher";

    public abstract class SupervisionMessage extends WebConsoleServiceMessage{
        private static final long serialVersionUID = -7345274984446689526L;
        private final Instant timeStamp;
        private final String groupName;

        private SupervisionMessage(final SupervisionEvent event){
            timeStamp = event.getTimeStamp();
            groupName = event.getGroupName();
        }

        @JsonProperty("timeStamp")
        @JsonSerialize(using = InstantSerializer.class)
        public final Instant getTimeStamp(){
            return timeStamp;
        }

        @JsonProperty("groupName")
        public final String getGroupName(){
            return groupName;
        }
    }

    @JsonTypeName("healthStatusChanged")
    public final class GroupStatusChangedMessage extends SupervisionMessage{
        private static final long serialVersionUID = -9201166624972276258L;
        private final HealthStatus previousStatus;
        private final HealthStatus newStatus;
        private final String mostProblematicResource;

        private GroupStatusChangedMessage(final HealthStatusChangedEvent event) {
            super(event);
            this.previousStatus = event.getPreviousStatus().getSummaryStatus();
            this.newStatus = event.getNewStatus().getSummaryStatus();
            this.mostProblematicResource = ResourceGroupHealthStatus.getMostProblematicResource(event.getNewStatus()).orElse(null);
        }

        @JsonProperty("mostProblematicResource")
        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public String getMostProblematicResource(){ //may be null
            return mostProblematicResource;
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
    public final class GroupCompositionChangedMessage extends SupervisionMessage {
        private static final long serialVersionUID = 469376947661340798L;
        private final GroupCompositionChanged.Modifier modifier;
        private final String resourceName;

        private GroupCompositionChangedMessage(@Nonnull final GroupCompositionChanged event) {
            super(event);
            this.modifier = event.getModifier();
            this.resourceName = event.getResourceName();
        }

        @JsonProperty("modifier")
        @JsonSerialize(using = ModifierSerializer.class)
        public GroupCompositionChanged.Modifier getModifier() {
            return modifier;
        }

        @JsonProperty("resourceName")
        public String getResourceName() {
            return resourceName;
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

    @Override
    public void handle(@Nonnull final SupervisionEvent event, final Object handback) {
        final Consumer<? super WebMessage> broadcastMessageSender = this::sendBroadcastMessage;
        //send message when resource added or removed
        Convert.toType(event, GroupCompositionChanged.class)
                .map(GroupCompositionChangedMessage::new)
                .ifPresent(broadcastMessageSender);
        //health status was changed
        Convert.toType(event, HealthStatusChangedEvent.class)
                .map(GroupStatusChangedMessage::new)
                .ifPresent(broadcastMessageSender);
    }

    private static void resetHealthStatus(final SupervisorClient client) {
        try {
            client.queryObject(HealthStatusProvider.class).ifPresent(HealthStatusProvider::reset);
        } finally {
            client.close();
        }
    }

    @POST
    @Path("/groupStatus/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resetHealthStatus(final String groupName){
        SupervisorClient.tryCreate(getBundleContext(), groupName).ifPresent(ResourceGroupWatcherService::resetHealthStatus);
        return Response.noContent().build();
    }

    @Override
    public void close() throws Exception {
        Utils.closeAll(hub, super::close);
    }
}
