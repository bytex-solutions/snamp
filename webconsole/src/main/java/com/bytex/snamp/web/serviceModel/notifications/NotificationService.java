package com.bytex.snamp.web.serviceModel.notifications;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.notifications.NotificationBuilder;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.connector.notifications.Severity;
import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.json.NotificationSerializer;
import com.bytex.snamp.web.serviceModel.AbstractPrincipalBoundedService;
import com.bytex.snamp.web.serviceModel.RESTController;
import com.bytex.snamp.web.serviceModel.WebConsoleSession;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Provides delivery of all notifications to the web console.
 */
@Path("/")
public final class NotificationService extends AbstractPrincipalBoundedService<NotificationSettings> implements NotificationListener, RESTController {
    private static final String URL_CONTEXT = "/notifications";
    
    @JsonTypeName("resourceNotification")
    public final class NotificationMessage extends WebConsoleServiceMessage{
        private static final long serialVersionUID = -9189834497935677635L;
        private final Notification notification;
        private final Severity severity;

        private NotificationMessage(@Nonnull final Notification notification, @Nonnull final Severity severity) {
            this.notification = notification;
            this.severity = severity;
        }

        @JsonProperty
        @JsonSerialize(using = SeveritySerializer.class)
        public Severity getSeverity() {
            return severity;
        }

        @JsonSerialize(using = NotificationSerializer.class)
        @JsonProperty
        public Notification getNotification(){
            return notification;
        }
    }

    private static final class NotificationTypeAggregator extends HashSet<String> implements Consumer<NotificationSupport> {
        private static final long serialVersionUID = 2963785216575253227L;

        NotificationTypeAggregator(){
            super(30);
        }

        @Override
        public void accept(final NotificationSupport notifications) {
            for (final MBeanNotificationInfo notificationInfo : notifications.getNotificationInfo())
                Collections.addAll(this, notificationInfo.getNotifTypes());
        }
    }

    private final NotificationHub hub;

    public NotificationService() {
        super(NotificationSettings.class);
        hub = new NotificationHub();
    }

    private void handleNotification(final WebConsoleSession session, final Notification notification, final Severity severity) {
        final NotificationSettings settings = getUserData(session);
        if (settings.isNotificationEnabled(notification, severity))
            session.sendMessage(new NotificationMessage(notification, severity));
    }

    @Override
    public void handleNotification(final NotificationEvent event) {
        final Notification notification = new NotificationBuilder(event.getNotification()).setSource(event.getResourceName()).get();
        final Severity severity = NotificationDescriptor.getSeverity(event.getMetadata());
        forEachSession(session -> handleNotification(session, notification, severity));
    }

    @Override
    protected void initialize() {
        try {
            hub.startTracking(this);
        } catch (final Exception e) {
            getLogger().log(Level.SEVERE, "Unable to start notification listener service", e);
        }
    }

    /**
     * Gets URL context of the service.
     *
     * @return URL context of the service.
     */
    @Override
    public String getUrlContext() {
        return URL_CONTEXT;
    }

    @Nonnull
    @Override
    protected NotificationSettings createUserData() {
        return new NotificationSettings();
    }

    @Path("/types")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Set<String> getAvailableNotifications() {
        final NotificationTypeAggregator notificationTypes = new NotificationTypeAggregator();
        final BundleContext context = getBundleContext();
        for (final String resourceName : ManagedResourceConnectorClient.selector().getResources(context))
            ManagedResourceConnectorClient.tryCreate(context, resourceName).ifPresent(client -> {
                try {
                    client.queryObject(NotificationSupport.class).ifPresent(notificationTypes);
                } finally {
                    client.close();
                }
            });
        return notificationTypes;
    }

    @Override
    public void close() throws Exception {
        Utils.closeAll(hub, super::close);
    }
}
