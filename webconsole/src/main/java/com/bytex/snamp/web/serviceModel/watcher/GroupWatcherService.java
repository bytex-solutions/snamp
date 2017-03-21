package com.bytex.snamp.web.serviceModel.watcher;

import com.bytex.snamp.connector.supervision.GroupStatusChangedEvent;
import com.bytex.snamp.connector.supervision.GroupStatusEventListener;
import com.bytex.snamp.connector.supervision.HealthStatus;
import com.bytex.snamp.connector.supervision.HealthSupervisor;
import com.bytex.snamp.web.serviceModel.AbstractWebConsoleService;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

/**
 * Represents notification service.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
@Path("/")
public final class GroupWatcherService extends AbstractWebConsoleService implements GroupStatusEventListener {
    public static final String NAME = "groupWatcher";
    public static final String URL_CONTEXT = '/' + NAME;

    @JsonTypeName("groupStatusChanged")
    public final class GroupStatusChangedMessage extends WebConsoleServiceMessage{
        private static final long serialVersionUID = -9201166624972276258L;
        private final HealthStatus previousStatus;
        private final HealthStatus newStatus;

        private GroupStatusChangedMessage(final GroupStatusChangedEvent event) {
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
    }

    private final HealthSupervisor supervisor;

    public GroupWatcherService(final HealthSupervisor supervisor){
        this.supervisor = Objects.requireNonNull(supervisor);
    }

    @Override
    protected void initialize() {
        supervisor.addHealthStatusEventListener(this);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/groups")
    public Set<String> getGroups(){
        return supervisor.getWatchingGroups();
    }

    @GET
    @Path("/groups/status")
    public StatusOfGroups getStatus() {
        final StatusOfGroups result = new StatusOfGroups();
        for (final String groupName : supervisor.getWatchingGroups()) {
            final HealthStatus status = supervisor.getHealthStatus(groupName);
            if (status != null)
                result.put(groupName, status);
        }
        return result;
    }

    @Override
    public void statusChanged(final GroupStatusChangedEvent event) {
        sendBroadcastMessage(new GroupStatusChangedMessage(event));
    }

    @Override
    public void close() throws Exception {
        supervisor.removeHealthStatusEventListener(this);
        super.close();
    }
}
