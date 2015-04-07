package com.itworks.snamp.adapters.ssh;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.adapters.*;
import com.itworks.snamp.concurrent.ThreadSafeObject;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.CustomAttributeInfo;
import com.itworks.snamp.connectors.notifications.NotificationBox;
import com.itworks.snamp.connectors.notifications.NotificationDescriptor;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.TabularDataUtils;
import com.itworks.snamp.jmx.WellKnownType;
import com.itworks.snamp.jmx.json.*;
import net.schmizz.sshj.userauth.keyprovider.*;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.auth.CachingPublicKeyAuthenticator;
import org.apache.sshd.server.jaas.JaasPasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;

import javax.management.*;
import javax.management.openmbean.*;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.*;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.itworks.snamp.adapters.ssh.SshAdapterConfigurationDescriptor.*;

/**
 * Represents SSH resource adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SshAdapter extends AbstractResourceAdapter implements AdapterController {
    static final String NAME = SshHelpers.ADAPTER_NAME;
    private static Gson FORMATTER = Formatters.enableAll(new GsonBuilder()).create();

    private static final class SshNotificationMappingImpl extends NotificationRouter implements SshNotificationMapping {
        private static final Gson formatter = new GsonBuilder()
                .registerTypeHierarchyAdapter(Notification.class, new NotificationSerializer())
                .registerTypeAdapter(ObjectName.class, new ObjectNameFormatter())
                .registerTypeHierarchyAdapter(CompositeData.class, new CompositeDataFormatter())
                .registerTypeHierarchyAdapter(TabularData.class, new TabularDataFormatter()).create();

        private final String resourceName;

        private SshNotificationMappingImpl(final MBeanNotificationInfo metadata,
                                           final NotificationBox mailbox,
                                           final String resourceName){
            super(metadata, mailbox);
            this.resourceName = resourceName;
        }

        @Override
        public void print(final Notification notif, final Writer output) {
            notif.setSource(resourceName);
            formatter.toJson(notif, output);
        }
    }

    private static final class SshNotificationsModel extends ThreadSafeObject{
        private final Map<String, ResourceNotificationList<SshNotificationMappingImpl>> notifications;
        private final NotificationBox mailbox;

        private SshNotificationsModel(){
            notifications = createNotifs();
            mailbox = new NotificationBox(50);
        }

        private static Map<String, ResourceNotificationList<SshNotificationMappingImpl>> createNotifs(){
            return new HashMap<String, ResourceNotificationList<SshNotificationMappingImpl>>(20){
                private static final long serialVersionUID = 3091347160169529722L;

                @Override
                public void clear() {
                    for(final ResourceNotificationList<?> list: values())
                        list.clear();
                    super.clear();
                }
            };
        }

        private void clear(){
            try(final LockScope ignored = beginWrite()){
                notifications.clear();
            }
            mailbox.clear();
        }

        private Notification poll(final String resourceName) {
            final Notification notif = mailbox.poll();
            return notif != null && Objects.equals(resourceName, notif.getSource()) ? notif : null;
        }

        private Notification poll() {
            return mailbox.poll();
        }

        private Set<String> getResourceNotifications(final String resourceName) {
            try(final LockScope ignored = beginRead()){
                if(notifications.containsKey(resourceName)){
                    final ResourceNotificationList<?> notifs = notifications.get(resourceName);
                    return notifs != null ? notifs.keySet() : ImmutableSet.<String>of();
                }
                else return ImmutableSet.of();
            }
        }

        private NotificationAccessor addNotification(final String resourceName,
                                                     final MBeanNotificationInfo metadata){
            try(final LockScope ignored = beginWrite()){
                final ResourceNotificationList<SshNotificationMappingImpl> list;
                if(notifications.containsKey(resourceName))
                    list = notifications.get(resourceName);
                else notifications.put(resourceName, list = new ResourceNotificationList<>());
                final SshNotificationMappingImpl accessor = new SshNotificationMappingImpl(metadata, mailbox, resourceName);
                list.put(accessor);
                return accessor;
            }
        }

        private Iterable<? extends NotificationAccessor> clear(final String resourceName){
            try(final LockScope ignored = beginWrite()){
                return notifications.containsKey(resourceName) ?
                    notifications.remove(resourceName).values():
                        ImmutableList.<SshNotificationMappingImpl>of();
            }
        }

        private NotificationAccessor removeNotification(final String resourceName,
                                                        final MBeanNotificationInfo metadata){
            try(final LockScope ignored = beginWrite()){
                final ResourceNotificationList<SshNotificationMappingImpl> list;
                if(notifications.containsKey(resourceName))
                    list = notifications.get(resourceName);
                else return null;
                final NotificationAccessor result = list.remove(metadata);
                if(list.isEmpty())
                    notifications.remove(resourceName);
                return result;
            }
        }

        private <E extends Exception> boolean processNotification(final String resourceName,
                                                                 final String notificationID,
                                                                 final Consumer<? super SshNotificationMapping, E> handler) throws E{
            try(final LockScope ignored = beginRead()){
                if(notifications.containsKey(resourceName)){
                    final ResourceNotificationList<SshNotificationMappingImpl> list = notifications.get(resourceName);
                    final SshNotificationMappingImpl mapping = list.get(notificationID);
                    if(mapping != null){
                        handler.accept(mapping);
                        return true;
                    }
                    else return false;
                }
                else return false;
            }
        }
    }

    private abstract static class AbstractSshAttributeMapping extends AttributeAccessor implements SshAttributeMapping {
        private AbstractSshAttributeMapping(final MBeanAttributeInfo metadata){
            super(metadata);
        }

        @Override
        public final String getOriginalName() {
            return AttributeDescriptor.getAttributeName(getMetadata());
        }

        private void printValueAsJson(final Writer output) throws IOException, JMException{
            FORMATTER.toJson(getValue(), output);
            output.flush();
        }

        @Override
        public final void printValue(final Writer output, final AttributeValueFormat format) throws JMException, IOException {
            switch (format){
                case JSON: printValueAsJson(output); return;
                default: printValueAsText(output);
            }
        }

        protected abstract void printValueAsText(final Writer output) throws JMException, IOException;

        /**
         * Prints attribute value.
         *
         * @param input An input stream that contains attribute
         * @throws javax.management.JMException
         */
        @Override
        public final void setValue(final Reader input) throws JMException, IOException {
            if(getType() != null)
                setValue(FORMATTER.fromJson(input, getType().getJavaType()));
            else throw new UnsupportedOperationException(String.format("Attribute %s is read-only", getName()));
        }

        @Override
        public final void printOptions(final Writer output) throws IOException{
            final Descriptor descr = getMetadata().getDescriptor();
            for(final String fieldName: descr.getFieldNames())
                output.append(String.format("%s = %s", fieldName, descr.getFieldValue(fieldName)));
            output.flush();
        }
    }

    private static final class ReadOnlyAttributeMapping extends AbstractSshAttributeMapping {
        private ReadOnlyAttributeMapping(final MBeanAttributeInfo metadata){
            super(metadata);
        }

        @Override
        protected void printValueAsText(final Writer output) throws JMException, IOException {
            output.append(Objects.toString(getValue(Object.class), "NULL"));
        }

        @Override
        public boolean canWrite() {
            return false;
        }
    }

    private static final class DefaultAttributeMapping extends AbstractSshAttributeMapping {

        private DefaultAttributeMapping(final MBeanAttributeInfo metadata) {
            super(metadata);
        }

        @Override
        protected void printValueAsText(final Writer output) throws JMException, IOException {
            output.append(Objects.toString(getValue(), ""));
        }
    }

    private static abstract class AbstractBufferAttributeMapping<B extends Buffer> extends AbstractSshAttributeMapping {
        protected static final char WHITESPACE = ' ';
        private final Class<B> bufferType;

        private AbstractBufferAttributeMapping(final MBeanAttributeInfo metadata,
                                               final Class<B> bufferType){
            super(metadata);
            this.bufferType = bufferType;
        }

        protected abstract void printValueAsText(final B buffer, final Writer output) throws IOException;

        @Override
        protected final void printValueAsText(final Writer output) throws JMException, IOException {
            printValueAsText(getValue(bufferType), output);
        }
    }

    private static final class ByteBufferAttributeMapping extends AbstractBufferAttributeMapping<ByteBuffer> {
        private ByteBufferAttributeMapping(final MBeanAttributeInfo metadata){
            super(metadata, ByteBuffer.class);
        }

        @Override
        protected void printValueAsText(final ByteBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining()) {
                output.append(Byte.toString(buffer.get()));
                output.append(WHITESPACE);
            }
        }
    }

    private static final class CharBufferAttributeMapping extends AbstractBufferAttributeMapping<CharBuffer> {
        private CharBufferAttributeMapping(final MBeanAttributeInfo metadata){
            super(metadata, CharBuffer.class);
        }

        @Override
        protected void printValueAsText(final CharBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining()) {
                output.append(buffer.get());
                output.append(WHITESPACE);
            }
        }
    }

    private static final class ShortBufferAttributeMapping extends AbstractBufferAttributeMapping<ShortBuffer> {
        private ShortBufferAttributeMapping(final MBeanAttributeInfo metadata){
            super(metadata, ShortBuffer.class);
        }

        @Override
        protected void printValueAsText(final ShortBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining()) {
                output.append(Short.toString(buffer.get()));
                output.append(WHITESPACE);
            }
        }
    }

    private static final class IntBufferAttributeMapping extends AbstractBufferAttributeMapping<IntBuffer> {
        private IntBufferAttributeMapping(final MBeanAttributeInfo metadata){
            super(metadata, IntBuffer.class);
        }

        @Override
        protected void printValueAsText(final IntBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining()) {
                output.append(Integer.toString(buffer.get()));
                output.append(WHITESPACE);
            }
        }
    }

    private static final class LongBufferAttributeMapping extends AbstractBufferAttributeMapping<LongBuffer> {
        private LongBufferAttributeMapping(final MBeanAttributeInfo metadata){
            super(metadata, LongBuffer.class);
        }

        @Override
        protected void printValueAsText(final LongBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining()) {
                output.append(Long.toString(buffer.get()));
                output.append(WHITESPACE);
            }
        }
    }

    private static final class FloatBufferAttributeMapping extends AbstractBufferAttributeMapping<FloatBuffer> {
        private FloatBufferAttributeMapping(final MBeanAttributeInfo metadata){
            super(metadata, FloatBuffer.class);
        }

        @Override
        protected void printValueAsText(final FloatBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining()) {
                output.append(Float.toString(buffer.get()));
                output.append(WHITESPACE);
            }
        }
    }

    private static final class DoubleBufferAttributeMapping extends AbstractBufferAttributeMapping<DoubleBuffer> {
        private DoubleBufferAttributeMapping(final MBeanAttributeInfo metadata){
            super(metadata, DoubleBuffer.class);
        }

        @Override
        protected void printValueAsText(final DoubleBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining()) {
                output.append(Double.toString(buffer.get()));
                output.append(WHITESPACE);
            }
        }
    }

    private static final class CompositeDataAttributeMapping extends AbstractSshAttributeMapping {
        private CompositeDataAttributeMapping(final MBeanAttributeInfo metadata){
            super(metadata);
        }

        @Override
        protected void printValueAsText(final Writer output) throws JMException, IOException {
            final CompositeData value = getValue(CompositeData.class);
            for(final String key: value.getCompositeType().keySet()){
                output.append(String.format("%s = %s", key, FORMATTER.toJson(value.get(key))));
                output.append(System.lineSeparator());
            }
        }
    }

    private static final class TabularDataAttributeMapping extends AbstractSshAttributeMapping {
        private TabularDataAttributeMapping(final MBeanAttributeInfo metadata){
            super(metadata);
        }

        private static String joinString(final Collection<?> values,
                                  final String format,
                                  final String separator) {
            return Joiner.on(separator).join(Collections2.transform(values, new Function<Object, String>() {
                @Override
                public final String apply(final Object input) {
                    return String.format(format, input);
                }
            }));
        }

        @Override
        protected void printValueAsText(final Writer output) throws JMException, IOException {
            final TabularData data = getValue(TabularData.class);
            final String COLUMN_SEPARATOR = "\t";
            final String ITEM_FORMAT = "%-10s";
            //print column first
            output.append(joinString(data.getTabularType().getRowType().keySet(), ITEM_FORMAT, COLUMN_SEPARATOR));
            //print rows
            TabularDataUtils.forEachRow(data, new Consumer<CompositeData, IOException>() {
                @Override
                public void accept(final CompositeData row) throws IOException{
                    final Collection<String> values = Collections2.transform(row.values(), new JsonSerializerFunction(FORMATTER));
                    output.append(joinString(values, ITEM_FORMAT, COLUMN_SEPARATOR));
                }
            });
        }
    }

    private static final class SshAttributesModel extends AbstractAttributesModel<AbstractSshAttributeMapping>{

        @Override
        protected AbstractSshAttributeMapping createAccessor(final MBeanAttributeInfo metadata) {
            final WellKnownType attributeType = CustomAttributeInfo.getType(metadata);
            if(attributeType != null)
                switch (attributeType){
                    case BYTE_BUFFER:
                        return new ByteBufferAttributeMapping(metadata);
                    case CHAR_BUFFER:
                        return new CharBufferAttributeMapping(metadata);
                    case SHORT_BUFFER:
                        return new ShortBufferAttributeMapping(metadata);
                    case INT_BUFFER:
                        return new IntBufferAttributeMapping(metadata);
                    case LONG_BUFFER:
                        return new LongBufferAttributeMapping(metadata);
                    case FLOAT_BUFFER:
                        return new FloatBufferAttributeMapping(metadata);
                    case DOUBLE_BUFFER:
                        return new DoubleBufferAttributeMapping(metadata);
                    case DICTIONARY:
                        return new CompositeDataAttributeMapping(metadata);
                    case TABLE:
                        return new TabularDataAttributeMapping(metadata);
                    default:
                        return new DefaultAttributeMapping(metadata);
                }
            return new ReadOnlyAttributeMapping(metadata);
        }
    }

    private SshServer server;
    private ExecutorService threadPool;
    private final SshAttributesModel attributes;
    private final SshNotificationsModel notifications;

    SshAdapter(final String adapterInstanceName) {
        super(adapterInstanceName);
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

    private static SshSecuritySettings createSecuritySettings(final Map<String, String> parameters){
        return new SshSecuritySettings() {
            @Override
            public String getUserName() {
                return parameters.get(USER_NAME_PARAM);
            }

            @Override
            public String getPassword() {
                return parameters.get(PASSWORD_PARAM);
            }

            @Override
            public boolean hasUserCredentials() {
                return parameters.containsKey(USER_NAME_PARAM) && parameters.containsKey(PASSWORD_PARAM);
            }

            @Override
            public String getJaasDomain() {
                return parameters.get(JAAS_DOMAIN_PARAM);
            }

            @Override
            public boolean hasJaasDomain() {
                return parameters.containsKey(JAAS_DOMAIN_PARAM);
            }

            @Override
            public boolean hasClientPublicKey() {
                return parameters.containsKey(PUBLIC_KEY_FILE_PARAM);
            }

            @Override
            public PublicKey getClientPublicKey() {
                final File keyFile = new File(parameters.get(PUBLIC_KEY_FILE_PARAM));
                KeyFormat format = getClientPublicKeyFormat();
                try {
                    if (format == KeyFormat.Unknown)
                        format = KeyProviderUtil.detectKeyFileFormat(keyFile);
                    final FileKeyProvider provider;
                    switch (format) {
                        case PKCS8:
                            provider = new PKCS8KeyFile();
                            break;
                        case OpenSSH:
                            provider = new OpenSSHKeyFile();
                            break;
                        case PuTTY:
                            provider = new PuTTYKeyFile();
                            break;
                        default:
                            throw new IOException("Unknown public key format.");
                    }
                    provider.init(keyFile);
                    return provider.getPublic();
                } catch (final IOException e) {
                    SshHelpers.log(Level.WARNING, "Invalid SSH public key file.", e);
                }
                return null;
            }

            @Override
            public KeyFormat getClientPublicKeyFormat() {
                if (parameters.containsKey(PUBLIC_KEY_FILE_FORMAT_PARAM))
                    switch (parameters.get(PUBLIC_KEY_FILE_FORMAT_PARAM).toLowerCase()) {
                        case "pkcs8":
                            return KeyFormat.PKCS8;
                        case "openssh":
                            return KeyFormat.OpenSSH;
                        case "putty":
                            return KeyFormat.PuTTY;
                    }
                return KeyFormat.Unknown;
            }
        };
    }

    private void start(final String host,
                       final int port,
                       final String serverCertificateFile,
                       final SshSecuritySettings security,
                       final Supplier<ExecutorService> threadPoolFactory) throws Exception{
        final SshServer server = SshServer.setUpDefaultServer();
        final ExecutorService commandExecutors = threadPoolFactory.get();
        server.setHost(host);
        server.setPort(port);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(serverCertificateFile));
        setupSecurity(server, security);
        server.setShellFactory(ManagementShell.createFactory(this, commandExecutors));
        server.setCommandFactory(new CommandFactory() {
            private final AdapterController controller = Utils.weakReference(SshAdapter.this, AdapterController.class);

            @Override
            public Command createCommand(final String commandLine) {
                return commandLine != null && commandLine.length() > 0 ?
                        ManagementShell.createSshCommand(commandLine, controller, commandExecutors) :
                        null;
            }
        });
        server.start();
        this.server = server;
        this.threadPool = commandExecutors;
    }

    @Override
    protected void start(final Map<String, String> parameters) throws Exception {
        final String host = parameters.containsKey(HOST_PARAM) ?
                parameters.get(HOST_PARAM) :
                DEFAULT_HOST;
        final int port = parameters.containsKey(PORT_PARAM) ?
                Integer.parseInt(parameters.get(PORT_PARAM)) :
                DEFAULT_PORT;
        final String certificateFile = parameters.containsKey(CERTIFICATE_FILE_PARAM) ?
                parameters.get(CERTIFICATE_FILE_PARAM) :
                DEFAULT_CERTIFICATE;
        start(host,
                port,
                certificateFile,
                createSecuritySettings(parameters),
                new SshThreadPoolConfig(getInstanceName(), parameters));
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
            threadPool.shutdownNow();
        }
        finally {
            attributes.clear();
            notifications.clear();
            server = null;
            threadPool = null;
        }
        System.gc();
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    public Logger getLogger() {
        return getLogger(NAME);
    }

    @Override
    public Set<String> getConnectedResources() {
        return attributes.getHostedResources();
    }

    /**
     * Gets IDs of attributes exposed by the specified managed resources.
     *
     * @param resourceName The name of the managed resource.
     * @return A collection of connected attributes.
     */
    @Override
    public Set<String> getAttributes(final String resourceName) {
        return attributes.getResourceAttributes(resourceName);
    }

    @Override
    public <E extends Exception> boolean processAttribute(final String resourceName,
                                                          final String attributeID,
                                                          final Consumer<? super SshAttributeMapping, E> handler) throws E {
        return attributes.processAttribute(resourceName, attributeID, handler);
    }

    @Override
    public Notification poll(final String resourceName) {
        return notifications.poll(resourceName);
    }

    @Override
    public Notification poll() {
        return notifications.poll();
    }

    /**
     * Gets a collection of available notifications.
     *
     * @param resourceName The name of the managed resource.
     * @return A collection of available notifications.
     */
    @Override
    public Set<String> getNotifications(final String resourceName) {
        return notifications.getResourceNotifications(resourceName);
    }

    @Override
    public <E extends Exception> boolean processNotification(final String resourceName,
                                                             final String notificationID,
                                                             final Consumer<? super SshNotificationMapping, E> handler) throws E {
        return notifications.processNotification(resourceName, notificationID, handler);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo, S> FeatureAccessor<M, S> addFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, S>)attributes.addAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M, S>)notifications.addNotification(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    @Override
    protected Iterable<? extends FeatureAccessor<?, ?>> removeAllFeatures(final String resourceName) {
        return Iterables.concat(attributes.clear(resourceName), notifications.clear(resourceName));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M, ?> removeFeature(final String resourceName, final M feature) {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, ?>)attributes.removeAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M, ?>)notifications.removeNotification(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }
}
