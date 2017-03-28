package com.bytex.snamp.web.serviceModel.notifications;

import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.json.NotificationSerializer;
import com.bytex.snamp.web.serviceModel.AbstractPrincipalBoundedService;
import com.bytex.snamp.web.serviceModel.WebConsoleSession;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.ws.rs.Path;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides delivery of all notifications to the web console.
 */
@Path("/")
public final class NotificationService extends AbstractPrincipalBoundedService<NotificationSettings> implements NotificationListener, ServiceListener {
    public static final String NAME = "notifications";
    public static final String URL_CONTEXT = '/' + NAME;

    @JsonTypeName("resourceNotification")
    public final class NotificationMessage extends WebConsoleServiceMessage{
        private static final long serialVersionUID = -9189834497935677635L;
        private final Notification notification;

        private NotificationMessage(final Notification notification) {
            this.notification = Objects.requireNonNull(notification);
        }

        @JsonSerialize(using = NotificationSerializer.class)
        public Notification getNotification(){
            return notification;
        }
    }

    private final ExecutorService threadPool;

    public NotificationService(final ExecutorService threadPool) {
        super(NotificationSettings.class);
        this.threadPool = Objects.requireNonNull(threadPool);
        ManagedResourceConnectorClient.filterBuilder().addServiceListener(getBundleContext(), this);
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForBundle(getBundleContext());
    }

    private void removeNotificationListener(final ManagedResourceConnectorClient client) {
        final NotificationSupport notifications = client.queryObject(NotificationSupport.class);
        if (notifications != null)
            try {
                notifications.removeNotificationListener(this);
            } catch (final ListenerNotFoundException e) {
                getLogger().log(Level.WARNING, e.getMessage(), e);
            }
    }

    private void addNotificationListener(final ManagedResourceConnectorClient client){
        final NotificationSupport notifications = client.queryObject(NotificationSupport.class);
        if (notifications != null)
            notifications.addNotificationListener(this, null, new NotificationSource(notifications, client.getManagedResourceName()));
    }

    @Override
    public void serviceChanged(final ServiceEvent event) {
        if (Utils.isInstanceOf(event.getServiceReference(), ManagedResourceConnector.class)) {
            final BundleContext context = getBundleContext();
            @SuppressWarnings("unchecked")
            final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(context, (ServiceReference<ManagedResourceConnector>) event.getServiceReference());
            try {
                switch (event.getType()) {
                    case ServiceEvent.UNREGISTERING:
                    case ServiceEvent.MODIFIED_ENDMATCH:
                        removeNotificationListener(client);
                        return;
                    case ServiceEvent.REGISTERED:
                        addNotificationListener(client);
                }
            } finally {
                client.release(context);
            }
        }
    }

    @Override
    protected void initialize() {
        final BundleContext context = getBundleContext();
        for (final String resourceName : ManagedResourceConnectorClient.getResources(context)) {
            final ManagedResourceConnectorClient client = ManagedResourceConnectorClient.tryCreate(context, resourceName);
            if (client != null)
                try {
                    addNotificationListener(client);
                } finally {
                    client.release(context);
                }
        }
    }

    @Nonnull
    @Override
    protected NotificationSettings createUserData() {
        return new NotificationSettings();
    }

    private void handleNotification(final WebConsoleSession session, final NotificationSource sender, final Notification notification) {
        notification.setSource(sender.getResourceName());
        final NotificationSettings settings = getUserData(session);
        if (settings.isNotificationEnabled(sender, notification))
            session.sendMessage(new NotificationMessage(notification));
    }

    private void handleNotification(final NotificationSource sender, final Notification notification) {
        forEachSession(this, (service, session) -> service.handleNotification(session, sender, notification), threadPool);
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        //handback is always of type NotificationSource. See addNotificationListener
        if (handback instanceof NotificationSource)
            handleNotification((NotificationSource) handback, notification);
    }

    @Override
    public void close() throws Exception {
        getBundleContext().removeServiceListener(this);
        super.close();
    }
}
