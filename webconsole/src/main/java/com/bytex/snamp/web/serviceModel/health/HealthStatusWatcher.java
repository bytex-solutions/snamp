package com.bytex.snamp.web.serviceModel.health;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.connector.health.HealthCheckSupport;
import com.bytex.snamp.connector.health.OkStatus;
import com.bytex.snamp.core.FilterBuilder;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.supervision.*;
import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.web.serviceModel.AbstractWebConsoleService;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Represents notification service.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
@Path("/")
public final class HealthStatusWatcher extends AbstractWebConsoleService implements HealthStatusEventListener, ServiceListener {
    public static final String NAME = "groupWatcher";
    public static final String URL_CONTEXT = '/' + NAME;

    @JsonTypeName("groupStatusChanged")
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

    public HealthStatusWatcher(){
        SupervisorClient.filterBuilder().addServiceListener(getBundleContext(), this);
    }

    private void removeHealthStatusListener(final Supervisor supervisor) {
        Aggregator.queryAndAccept(supervisor, HealthStatusProvider.class, provider -> provider.removeHealthStatusEventListener(this));
    }

    private void addHealthStatusListener(final Supervisor supervisor) {
        Aggregator.queryAndAccept(supervisor, HealthStatusProvider.class, provider -> provider.addHealthStatusEventListener(this));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void serviceChanged(final ServiceEvent event) {
        if (Utils.isInstanceOf(event.getServiceReference(), Supervisor.class))
            try (final SupervisorClient client = new SupervisorClient(getBundleContext(), (ServiceReference<Supervisor>) event.getServiceReference())) {
                switch (event.getType()) {
                    case ServiceEvent.MODIFIED_ENDMATCH:
                    case ServiceEvent.UNREGISTERING:
                        removeHealthStatusListener(client);
                        return;
                    case ServiceEvent.REGISTERED:
                        addHealthStatusListener(client);
                        return;
                    default:
                        getLogger().warning(String.format("Unknown event type %s detected by HealthStatusWatcher", event.getType()));
                }
            }
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    @Override
    protected void initialize() {
        for (final String groupName : getGroups()) {
            final SupervisorClient client = SupervisorClient.tryCreate(getBundleContext(), groupName);
            if (client == null)
                getLogger().warning(String.format("Supervisor for group %s cannot be resolved", groupName));
            else
                addHealthStatusListener(client);
        }
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForBundle(getBundleContext());
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
        getBundleContext().removeServiceListener(this);
        super.close();
    }
}
