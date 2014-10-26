package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.SimpleTable;
import com.itworks.snamp.Table;
import com.itworks.snamp.TypeLiterals;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;
import com.itworks.snamp.connectors.notifications.Notification;
import com.itworks.snamp.connectors.notifications.NotificationMetadata;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.collections4.map.AbstractReferenceMap;
import org.apache.commons.collections4.map.ReferenceMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.auth.CachingPublicKeyAuthenticator;
import org.apache.sshd.server.jaas.JaasPasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;

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
        private final Map<Long, NotificationListener> listeners = new ReferenceMap<>(AbstractReferenceMap.ReferenceStrength.HARD,
                AbstractReferenceMap.ReferenceStrength.WEAK);

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
                listener.handle(sender, notificationMetadata.getEventName(), notif);
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
                    output.println(String.format("ARRAY = %s", ArrayUtils.toString(value)));
                }

                private void printValue(final Map<String, Object> value, final PrintWriter output){
                    output.println("MAP");
                    for(final Map.Entry<String, Object> pair: value.entrySet())
                        output.println(String.format("%s = %s", pair.getKey(), pair.getValue()));
                }

                private String joinString(Collection<?> values,
                                               final String format,
                                               final String separator){
                    values = CollectionUtils.collect(values, new Transformer<Object, String>() {
                        @Override
                        public String transform(final Object input) {
                            return String.format(format, input);
                        }
                    });
                    return StringUtils.join(values, separator);
                }

                private void printValue(final Table<String> value,
                                        final boolean columnBasedView,
                                        final PrintWriter output){
                    output.println("TABLE");
                    output.println();
                    if(columnBasedView){
                        final List<String> columns = SimpleTable.getOrderedColumns(value);
                        final String COLUMN_SEPARATOR = "\t";
                        final String ITEM_FORMAT = "%-10s";
                        //print columns first
                        output.println(joinString(columns, ITEM_FORMAT, COLUMN_SEPARATOR));
                        //print rows
                        for(int row = 0; row < value.getRowCount(); row++){
                            output.println(joinString(SimpleTable.getRow(value, columns, row), ITEM_FORMAT, COLUMN_SEPARATOR));
                        }

                    }
                    else for(int i = 0; i < value.getRowCount(); i++){
                        output.println(String.format("ROW #%s:", i));
                        for(final String column: value.getColumns())
                            output.println(String.format("%s = %s", column, value.getCell(column, i)));
                        output.println();
                    }
                }

                @Override
                public void printValue(final PrintWriter output) throws TimeoutException {
                    final String VALUE_STUB = "<UNABLE TO DISPLAY VALUE>";
                    final Object attrValue = accessor.getValue(accessor.getWellKnownType(), null);
                    if(TypeLiterals.isInstance(attrValue, TypeLiterals.OBJECT_ARRAY))
                        printValue(TypeLiterals.cast(attrValue, TypeLiterals.OBJECT_ARRAY), output);
                    else if(TypeLiterals.isInstance(attrValue, TypeLiterals.STRING_MAP))
                        printValue(TypeLiterals.cast(attrValue, TypeLiterals.STRING_MAP), output);
                    else if(TypeLiterals.isInstance(attrValue, TypeLiterals.STRING_COLUMN_TABLE))
                        printValue(TypeLiterals.cast(attrValue, TypeLiterals.STRING_COLUMN_TABLE),
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

    SshAdapter(final String host,
               final int port,
               final String serverCertificateFile,
               final SshSecuritySettings security,
               final Map<String, ManagedResourceConfiguration> resources) {
        super(resources);
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
     *
     * @return {@literal true}, if adapter is started successfully; otherwise, {@literal false}.
     */
    @Override
    protected boolean start() {
        server.setShellFactory(ManagementShell.createFactory(this, commandExecutors, getLogger()));
        server.setCommandFactory(new CommandFactory() {
            private final AdapterController controller = SshAdapter.this;

            @Override
            public Command createCommand(final String commandLine) {
                return commandLine != null && commandLine.length() > 0 ?
                        ManagementShell.createSshCommand(commandLine, controller, commandExecutors, getLogger()) :
                        null;
            }
        });
        populateModel(attributes);
        populateModel(notifications);
        try {
            server.start();
            return true;
        } catch (final IOException e) {
            getLogger().log(Level.SEVERE, String.format("Unable to start SSH adapter"), e);
            return false;
        }
    }

    /**
     * Stops the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     */
    @Override
    protected void stop() {
        try {
            server.stop();
        }
        catch (final InterruptedException e) {
            getLogger().log(Level.SEVERE, String.format("Unable to stop SSH adapter"), e);
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
        return SshHelpers.getLogger();
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
