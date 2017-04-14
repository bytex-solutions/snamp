package com.bytex.snamp.web.serviceModel.health;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.connector.health.HealthCheckSupport;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.supervision.Supervisor;
import com.bytex.snamp.supervision.SupervisorClient;
import com.bytex.snamp.supervision.health.HealthStatusChangedEvent;
import com.bytex.snamp.supervision.health.HealthStatusEventListener;
import com.bytex.snamp.web.serviceModel.AbstractWebConsoleService;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

/**
 * Represents notification service.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
@Path("/")
public final class HealthStatusWatcher extends AbstractWebConsoleService implements HealthStatusEventListener {
    public static final String NAME = "health-watcher";
    public static final String URL_CONTEXT = '/' + NAME;

    @JsonTypeName("healthStatusChanged")
    public final class GroupStatusChangedMessage extends WebConsoleServiceMessage{
        private static final long serialVersionUID = -9201166624972276258L;
        private final HealthStatus previousStatus;
        private final HealthStatus newStatus;

        private GroupStatusChangedMessage(final HealthStatusChangedEvent event) {
            this.previousStatus = event.getPreviousStatus();
            this.newStatus = event.getNewStatus();
        }

        @JsonProperty
        @JsonSerialize(using = HealthStatusSerializer.class)
        public HealthStatus getPreviousStatus(){
            return previousStatus;
        }

        @JsonProperty
        @JsonSerialize(using = HealthStatusSerializer.class)
        public HealthStatus getNewStatus(){
            return newStatus;
        }
    }

    @JsonSerialize(contentUsing = HealthStatusSerializer.class)
    public static final class StatusOfGroups extends HashMap<String, HealthStatus>{
        private static final long serialVersionUID = 2645921325913575632L;

        void putStatus(final String groupName, final Supervisor supervisor) {
            final HealthStatus status = Aggregator.queryAndApply(supervisor, HealthCheckSupport.class, HealthCheckSupport::getStatus).orElseGet(OkStatus::new);
            put(groupName, status);
        }
    }

    private final HealthStatusEventHub hub;

    public HealthStatusWatcher(){
        hub = new HealthStatusEventHub();
    }

    @Override
    protected void initialize() {
        try {
            hub.startTracking(this);
        } catch (final Exception e) {
            getLogger().log(Level.SEVERE, "Unable to start tracking health status", e);
        }
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
    public StatusOfGroups getStatus() {
        final StatusOfGroups result = new StatusOfGroups();
        for(final String groupName: getGroups()) {
            final SupervisorClient client = SupervisorClient.tryCreate(getBundleContext(), groupName);
            if (client == null)
                getLogger().warning(String.format("Supervisor for group %s cannot be resolved", groupName));
            else try {
                result.putStatus(groupName, client);
            } finally {
                client.close();
            }
        }
        return result;
    }

    @Override
    public void statusChanged(final HealthStatusChangedEvent event) {
        sendBroadcastMessage(new GroupStatusChangedMessage(event));
    }

    @Override
    public void close() throws Exception {
        Utils.closeAll(hub, super::close);
    }
}
