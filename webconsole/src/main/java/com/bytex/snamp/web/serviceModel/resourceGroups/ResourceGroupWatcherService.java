package com.bytex.snamp.web.serviceModel.resourceGroups;

import com.bytex.snamp.Convert;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.json.InstantSerializer;
import com.bytex.snamp.json.RangeSerializer;
import com.bytex.snamp.supervision.*;
import com.bytex.snamp.supervision.elasticity.*;
import com.bytex.snamp.supervision.health.HealthStatusChangedEvent;
import com.bytex.snamp.supervision.health.HealthStatusProvider;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;
import com.bytex.snamp.web.serviceModel.AbstractWebConsoleService;
import com.bytex.snamp.web.serviceModel.RESTController;
import com.bytex.snamp.web.serviceModel.WebMessage;
import com.bytex.snamp.web.serviceModel.charts.HealthStatusSerializer;
import com.google.common.collect.Range;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.annotation.Nonnull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Represents operations to work with resource groups.
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

    public enum ScalingAction{
        UNKNOWN,
        SCALE_IN,
        SCALE_OUT,
        OUT_OF_SPACE
    }

    @JsonTypeName("scalingHappens")
    public final class ScalingHappensMessage extends SupervisionMessage{
        private static final long serialVersionUID = -3466679934786763774L;
        private final Map<String, Double> evaluationResult;
        private final double castingVote;
        private final ScalingAction action;

        private ScalingHappensMessage(final ScalingEvent event) {
            super(event);
            evaluationResult = event.getPolicyEvaluationResult();
            castingVote = event.getCastingVoteWeight();
            if(event instanceof ScaleInEvent)
                action = ScalingAction.SCALE_IN;
            else if(event instanceof ScaleOutEvent)
                action = ScalingAction.SCALE_OUT;
            else if(event instanceof MaxClusterSizeReachedEvent)
                action = ScalingAction.OUT_OF_SPACE;
            else
                action = ScalingAction.UNKNOWN;
        }

        @JsonProperty("castingVoteWeight")
        public double getCastingVoteWeight(){
            return castingVote;
        }

        @JsonProperty("evaluationResult")
        public Map<String, Double> getEvaluationResult(){
            return evaluationResult;
        }

        @JsonProperty("action")
        public ScalingAction getAction(){
            return action;
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

    /**
     * Group modification type.
     */
    public enum GroupCompositionModifier {
        /**
         * Resource was added to the group.
         */
        ADDED,

        /**
         * Resource was removed from the group.
         */
        REMOVED,

        /**
         * Unknown action.
         */
        UNKNOWN,
    }

    @JsonTypeName("groupCompositionChanged")
    public final class GroupCompositionChangedMessage extends SupervisionMessage {
        private static final long serialVersionUID = 469376947661340798L;
        private final GroupCompositionModifier modifier;
        private final String resourceName;

        private GroupCompositionChangedMessage(@Nonnull final GroupCompositionChangedEvent event) {
            super(event);
            if (event instanceof ResourceAddedEvent)
                modifier = GroupCompositionModifier.ADDED;
            else if (event instanceof ResourceRemovedEvent)
                modifier = GroupCompositionModifier.REMOVED;
            else
                modifier = GroupCompositionModifier.UNKNOWN;
            this.resourceName = event.getResourceName();
        }

        @JsonProperty("modifier")
        public GroupCompositionModifier getModifier() {
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
        Convert.toType(event, GroupCompositionChangedEvent.class)
                .map(GroupCompositionChangedMessage::new)
                .ifPresent(broadcastMessageSender);
        //health status was changed
        Convert.toType(event, HealthStatusChangedEvent.class)
                .map(GroupStatusChangedMessage::new)
                .ifPresent(broadcastMessageSender);
        //scaling event
        Convert.toType(event, ScalingEvent.class)
                .map(ScalingHappensMessage::new)
                .ifPresent(broadcastMessageSender);
    }

    private static Response notFound(){
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    private static Response resetHealthStatus(final SupervisorClient client) {
        try {
            client.queryObject(HealthStatusProvider.class).ifPresent(HealthStatusProvider::reset);
        } finally {
            client.close();
        }
        return Response.noContent().build();
    }

    private static Response resetElasticity(final SupervisorClient client){
        try {
            client.queryObject(ElasticityManager.class).ifPresent(ElasticityManager::reset);
        } finally {
            client.close();
        }
        return Response.noContent().build();
    }

    @POST
    @Path("/{groupName}/groupStatus/reset")
    public Response resetHealthStatus(@PathParam("groupName") final String groupName) {
        return SupervisorClient.tryCreate(getBundleContext(), groupName)
                .map(ResourceGroupWatcherService::resetHealthStatus)
                .orElseGet(ResourceGroupWatcherService::notFound);
    }

    @POST
    @Path("/{groupName}/elasticity/reset")
    public Response resetElasticity(@PathParam("groupName") final String groupName) {
        return SupervisorClient.tryCreate(getBundleContext(), groupName)
                .map(ResourceGroupWatcherService::resetElasticity)
                .orElseGet(ResourceGroupWatcherService::notFound);
    }

    private static Range<Double> getRecommendation(final SupervisorClient client, final String policyName) {
        try {
            return client.queryObject(ElasticityManager.class)
                    .map(manager -> {
                        final ScalingPolicy policy = manager.getPolicies().get(policyName);
                        if (policy == null)
                            throw new WebApplicationException(Response.Status.NOT_FOUND);
                        else
                            return Convert.toType(policy, AttributeBasedScalingPolicy.class)
                                    .map(AttributeBasedScalingPolicy::getRecommendation)
                                    .<WebApplicationException>orElseThrow(() -> new WebApplicationException(Response.Status.BAD_REQUEST));
                    })
                    .orElseThrow(() -> new WebApplicationException(501));
        } finally {
            client.close();
        }
    }

    @GET
    @Path("/{groupName}/scaling-policies/attribute-based/{policyName}/recommendation")
    @Produces(MediaType.APPLICATION_JSON)
    @JsonSerialize(using = RangeSerializer.class)
    //501 error code if group supervisor doesn't provide elasticity manager
    //400 error code if specified scaling policy name has invalid type (attribute-based policy is expected)
    //404 if group supervisor or policy doesn't exist
    public Range<Double> getRecommendation(@PathParam("groupName") final String groupName,
                                           @PathParam("policyName") final String policyName) {
        return SupervisorClient.tryCreate(getBundleContext(), groupName)
                .map(client -> getRecommendation(client, policyName))
                .orElseThrow(() -> new WebApplicationException(notFound()));
    }

    @Override
    public void close() throws Exception {
        Utils.closeAll(hub, super::close);
    }
}
