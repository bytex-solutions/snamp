package com.itworks.snamp.adapters.ssh;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.MapMaker;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.connectors.notifications.NotificationMetadata;
import com.itworks.snamp.mapping.*;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.auth.CachingPublicKeyAuthenticator;
import org.apache.sshd.server.jaas.JaasPasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;

import java.io.PrintWriter;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Represents SSH resource adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SshAdapter extends AbstractResourceAdapter implements AdapterController {
    static final String NAME = SshHelpers.ADAPTER_NAME;

    private static final class SshNotificationsModel extends AbstractNotificationsModel<SshNotificationView>{
        private final AtomicLong idCounter = new AtomicLong(0L);
        private final Map<Long, NotificationListener> listeners;

        private SshNotificationsModel() {
            listeners = new MapMaker().weakValues().initialCapacity(10).makeMap();
        }

        /**
         * Creates a new notification metadata representation.
         *
         * @param resourceName User-defined name of the managed resource.
         * @param eventName    The resource-local identifier of the event.
         * @param notifMeta    The notification metadata to wrap.
         * @return A new notification metadata representation.
         */
        @Override
        protected SshNotificationView createNotificationView(final String resourceName, final String eventName, final NotificationMetadata notifMeta) {
            return new SshNotificationView() {
                @Override
                public String getEventName() {
                    return eventName;
                }

                @Override
                public String getResourceName() {
                    return resourceName;
                }
            };
        }

        /**
         * Processes SNAMP notification.
         *
         * @param sender               The name of the managed resource which emits the notification.
         * @param notif                The notification to process.
         * @param notificationMetadata The metadata of the notification.
         */
        @Override
        protected void handleNotification(final String sender, final Notification notif, final SshNotificationView notificationMetadata) {
            for(final NotificationListener listener: listeners.values())
                listener.handle(notificationMetadata, notif);
        }

        long addNotificationListener(final NotificationListener listener) {
            final long listenerID;
            listeners.put(listenerID = idCounter.incrementAndGet(),
                    Objects.requireNonNull(listener));
            return listenerID;
        }

        void removeNotificationListener(final long listenerID) {
            listeners.remove(listenerID);
        }

        /**
         * Creates subscription list ID.
         *
         * @param resourceName User-defined name of the managed resource which can emit the notification.
         * @param eventName    User-defined name of the event.
         * @return A new unique subscription list ID.
         */
        @Override
        protected String makeSubscriptionListID(final String resourceName, final String eventName) {
            return String.format("%s/%s", resourceName, eventName);
        }

        Set<String> getNotifications(final String resourceName) {
            final Set<String> notifs = new HashSet<>(size());
            for (final String notifID : keySet())
                if (notifID.startsWith(resourceName))
                    notifs.add(notifID);
            return notifs;
        }
    }

    private static final class SshAttributesModel extends AbstractAttributesModel<SshAttributeView>{

        /**
         * Creates a new domain-specific representation of the management attribute.
         *
         * @param resourceName             User-defined name of the managed resource.
         * @param userDefinedAttributeName User-defined name of the attribute.
         * @param accessor                 An accessor for the individual management attribute.
         * @return A new domain-specific representation of the management attribute.
         */
        @Override
        protected SshAttributeView createAttributeView(final String resourceName, final String userDefinedAttributeName, final AttributeAccessor accessor) {
            return new SshAttributeView() {
                private void printValue(final Object[] value, final PrintWriter output){
                    output.println(String.format("ARRAY = %s", Arrays.toString(value)));
                }

                private void printValue(final RecordSet<String, ?> value, final PrintWriter output){
                    output.println("MAP ");
                    final Map<String, ?> result = RecordSetUtils.toMap(value);
                    output.println(joinString(result.entrySet(), "%s", ", "));
                }

                private String joinString(Collection<?> values,
                                               final String format,
                                               final String separator) {
                    Collections2.transform(values, new Function<Object, String>() {
                        @Override
                        public final String apply(final Object input) {
                            return String.format(format, input);
                        }
                    });
                    return Joiner.on(separator).join(values);
                }

                private void printValue(final RowSet<?> value,
                                        final boolean columnBasedView,
                                        final PrintWriter output){
                    output.println("TABLE ");
                    output.println();
                    if(columnBasedView){
                        final String COLUMN_SEPARATOR = "\t";
                        final String ITEM_FORMAT = "%-10s";
                        //print columns first
                        output.println(joinString(value.getColumns(), ITEM_FORMAT, COLUMN_SEPARATOR));
                        //print rows
                        value.forEach(new RecordReader<Integer, RecordSet<String, ?>, ExceptionPlaceholder>() {
                            @Override
                            public void read(final Integer index, final RecordSet<String, ?> value) {
                                final Map<String, ?> row = RecordSetUtils.toMap(value);
                                output.println(joinString(row.values(), ITEM_FORMAT, COLUMN_SEPARATOR));
                            }
                        });
                    }
                    else value.sequential().forEach(new RecordReader<Integer, RecordSet<String, ?>, ExceptionPlaceholder>() {
                        @Override
                        public void read(final Integer index, final RecordSet<String, ?> value) {
                            output.println(String.format("ROW #%s:", index));
                            value.sequential().forEach(new RecordReader<String, Object, ExceptionPlaceholder>() {
                                @Override
                                public void read(final String column, final Object value) {
                                    output.println(String.format("%s = %s", column, value));
                                }
                            });
                            output.println();
                        }
                    });
                }

                @Override
                public void printValue(final PrintWriter output) throws TimeoutException {
                    final String VALUE_STUB = "<UNABLE TO DISPLAY VALUE>";
                    final Object attrValue = accessor.getValue(accessor.getWellKnownType(), null);
                    if(TypeLiterals.isInstance(attrValue, TypeLiterals.OBJECT_ARRAY))
                        printValue(TypeLiterals.cast(attrValue, TypeLiterals.OBJECT_ARRAY), output);
                    else if(TypeLiterals.isInstance(attrValue, TypeLiterals.NAMED_RECORD_SET))
                        printValue(TypeLiterals.cast(attrValue, TypeLiterals.NAMED_RECORD_SET), output);
                    else if(TypeLiterals.isInstance(attrValue, TypeLiterals.ROW_SET))
                        printValue(TypeLiterals.cast(attrValue, TypeLiterals.ROW_SET),
                                accessor.containsKey(SshAdapterConfigurationDescriptor.COLUMN_BASED_OUTPUT_PARAM),
                                output);
                    else output.println(Objects.toString(attrValue, VALUE_STUB));
                }

                @Override
                public String getName() {
                    return accessor.getName();
                }

                @Override
                public void printOptions(final PrintWriter output) {
                    for(final Map.Entry<String, String> option: accessor.entrySet())
                        output.println(String.format("%s = %s", option.getKey(), option.getValue()));
                }

                @Override
                public boolean canRead() {
                    return accessor.canRead();
                }

                @Override
                public boolean canWrite() {
                    return accessor.canWrite();
                }

                @Override
                public void setValue(final Object value) throws TimeoutException, AttributeSupportException {
                    accessor.setValue(value);
                }

                @Override
                public <I, O> O applyTransformation(final Class<? extends ValueTransformation<I, O>> transformation, final I arg) throws ReflectiveOperationException, TimeoutException, AttributeSupportException{
                    final ValueTransformation<I, O> t = transformation.newInstance();
                    return t.transform(arg, accessor);
                }
            };
        }

        /**
         * Creates a new unique identifier of the management attribute.
         * <p>
         * The identifier must be unique through all instances of the resource adapter.
         * </p>
         *
         * @param resourceName             User-defined name of the managed resource which supply the attribute.
         * @param userDefinedAttributeName User-defined name of the attribute.
         * @return A new unique identifier of the management attribute.
         */
        @Override
        protected String makeAttributeID(final String resourceName, final String userDefinedAttributeName) {
            return String.format("%s/%s", resourceName, userDefinedAttributeName);
        }

        private Set<String> byResourceName(final String resourceName) {
            final HashSet<String> attributes = new HashSet<>(size());
            for(final String attributeID: keySet())
                if(attributeID.startsWith(resourceName))
                    attributes.add(attributeID);
            return attributes;
        }
    }

    private final SshServer server;
    private final ExecutorService commandExecutors;
    private final SshAttributesModel attributes;
    private final SshNotificationsModel notifications;

    SshAdapter(final String adapterInstanceName,
                final String host,
               final int port,
               final String serverCertificateFile,
               final SshSecuritySettings security) {
        super(adapterInstanceName);
        server = SshServer.setUpDefaultServer();
        server.setHost(host);
        server.setPort(port);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(serverCertificateFile));
        setupSecurity(server, security);
        commandExecutors = Executors.newCachedThreadPool();
        attributes = new SshAttributesModel();
        notifications = new SshNotificationsModel();
    }

    private static void setupSecurity(final SshServer server, final SshSecuritySettings security) {
        if (security.hasJaasDomain()) {
            final JaasPasswordAuthenticator auth = new JaasPasswordAuthenticator();
            auth.setDomain(security.getJaasDomain());
            server.setPasswordAuthenticator(auth);
        } else if (security.hasUserCredentials())
            server.setPasswordAuthenticator(new PasswordAuthenticator() {
                private final String userName = security.getUserName();
                private final String password = security.getPassword();

                @Override
                public boolean authenticate(final String username, final String password, final ServerSession session) {
                    return Objects.equals(username, this.userName) && Objects.equals(password, this.password);
                }
            });
        if (security.hasClientPublicKey()) {
            server.setPublickeyAuthenticator(new CachingPublicKeyAuthenticator(new PublickeyAuthenticator() {
                private final PublicKey pk = security.getClientPublicKey();

                @Override
                public boolean authenticate(final String username, final PublicKey key, final ServerSession session) {
                    return Objects.equals(key, this.pk);
                }
            }));
        }
    }

    /**
     * Starts the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     */
    @Override
    protected void start() throws Exception {
        server.setShellFactory(ManagementShell.createFactory(this, commandExecutors));
        server.setCommandFactory(new CommandFactory() {
            private final AdapterController controller = SshAdapter.this;

            @Override
            public Command createCommand(final String commandLine) {
                return commandLine != null && commandLine.length() > 0 ?
                        ManagementShell.createSshCommand(commandLine, controller, commandExecutors) :
                        null;
            }
        });
        populateModel(attributes);
        populateModel(notifications);
        server.start();
    }

    /**
     * Stops the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     */
    @Override
    protected void stop() throws Exception{
        try {
            server.stop();
        }
        finally {
            clearModel(attributes);
            clearModel(notifications);
        }
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return getLogger(NAME);
    }

    @Override
    public Set<String> getConnectedResources() {
        return getHostedResources();
    }

    /**
     * Gets IDs of attributes exposed by the specified managed resources.
     *
     * @param resourceName The name of the managed resource.
     * @return A collection of connected attributes.
     */
    @Override
    public Set<String> getAttributes(final String resourceName) {
        return attributes.byResourceName(resourceName);
    }

    /**
     * Gets an attribute accessor.
     *
     * @param attributeID ID of the attribute.
     * @return The attribute accessor; or {@literal null}, if attribute doesn't exist.
     */
    @Override
    public SshAttributeView getAttribute(final String attributeID) {
        return attributes.get(attributeID);
    }

    @Override
    public long addNotificationListener(final NotificationListener listener) {
        return notifications.addNotificationListener(listener);
    }

    @Override
    public void removeNotificationListener(final long listenerID) {
        notifications.removeNotificationListener(listenerID);
    }

    /**
     * Gets a collection of available notifications.
     *
     * @param resourceName The name of the managed resource.
     * @return A collection of available notifications.
     */
    @Override
    public Set<String> getNotifications(final String resourceName) {
        return notifications.getNotifications(resourceName);
    }
}
