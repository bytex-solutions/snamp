package com.itworks.snamp.adapters.ssh;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.concurrent.ThreadSafeObject;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.notifications.NotificationBox;
import com.itworks.snamp.connectors.notifications.NotificationDescriptor;
import com.itworks.snamp.core.LogicalOperation;
import com.itworks.snamp.internal.AbstractKeyedObjects;
import com.itworks.snamp.internal.KeyedObjects;
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
import java.io.*;
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

    private static final class SshNotificationViewImpl extends NotificationBox implements SshNotificationView{
        private static final long serialVersionUID = -1887404933016444754L;
        private final MBeanNotificationInfo metadata;
        private final Gson formatter;
        private final String resourceName;

        private SshNotificationViewImpl(final MBeanNotificationInfo metadata,
                                        final String resourceName){
            super(20);
            this.metadata = metadata;
            this.resourceName = resourceName;
            GsonBuilder builder = new GsonBuilder()
                    .registerTypeHierarchyAdapter(Notification.class, new NotificationSerializer())
                    .registerTypeAdapter(ObjectName.class, new ObjectNameFormatter());
            final OpenType<?> userDataType = NotificationDescriptor.getUserDataType(metadata);
            if(userDataType instanceof CompositeType)
                builder = builder.registerTypeHierarchyAdapter(CompositeData.class, new CompositeDataSerializer());
            else if(userDataType instanceof TabularType)
                builder = builder.registerTypeHierarchyAdapter(TabularData.class, new TabularDataSerializer());
            else if(userDataType instanceof ArrayType<?> && ((ArrayType<?>)userDataType).getElementOpenType() instanceof CompositeType)
                builder = builder.registerTypeHierarchyAdapter(CompositeData[].class, new ArrayOfCompositeDataSerializer());
            else if(userDataType instanceof ArrayType<?> && ((ArrayType<?>)userDataType).getElementOpenType() instanceof TabularType)
                builder = builder.registerTypeHierarchyAdapter(TabularData[].class, new ArrayOfTabularDataSerializer());
            this.formatter = builder.create();
        }

        @Override
        public String getEventName() {
            return metadata.getNotifTypes()[0];
        }

        @Override
        public void print(final Notification notif, final Writer output) {
            notif.setSource(resourceName);
            formatter.toJson(notif, output);
        }
    }

    private static final class SshNotificationsModel extends ThreadSafeObject implements NotificationsModel{
        private final KeyedObjects<String, SshNotificationViewImpl> notifications;

        private SshNotificationsModel(){
            notifications = createNotifs();
        }

        private static KeyedObjects<String, SshNotificationViewImpl> createNotifs(){
            return new AbstractKeyedObjects<String, SshNotificationViewImpl>(10) {
                private static final long serialVersionUID = 2053672206296099383L;

                @Override
                public String getKey(final SshNotificationViewImpl item) {
                    return item.getEventName();
                }
            };
        }

        private static String makeListID(final String resourceName,
                                         final String userDefinedName){
            return resourceName + '/' + userDefinedName;
        }

        @Override
        public void addNotification(final String resourceName,
                                    final String userDefinedName,
                                    final String category,
                                    final NotificationConnector connector) {
            final String listID = makeListID(resourceName, userDefinedName);
            beginWrite();
            try{
                if(notifications.containsKey(listID)) return;
                notifications.put(new SshNotificationViewImpl(connector.enable(listID), resourceName));
            } catch (final JMException e) {
                SshHelpers.log(Level.SEVERE, "Failed to subscribe on %s notification", listID, e);
            } finally {
                endWrite();
            }
        }

        @Override
        public MBeanNotificationInfo removeNotification(final String resourceName,
                                                        final String userDefinedName,
                                                        final String category) {
            final String listID = makeListID(resourceName, userDefinedName);
            beginWrite();
            try{
                return notifications.containsKey(listID) ?
                        notifications.remove(listID).metadata:
                        null;
            }
            finally {
                endWrite();
            }
        }

        /**
         * Removes all notifications from this model.
         */
        @Override
        public void clear() {
            beginWrite();
            try{
                notifications.clear();
            }
            finally {
                endWrite();
            }
        }

        /**
         * Determines whether this model is empty.
         *
         * @return {@literal true}, if this model is empty; otherwise, {@literal false}.
         */
        @Override
        public boolean isEmpty() {
            beginRead();
            try {
                return notifications.isEmpty();
            }
            finally {
                endRead();
            }
        }

        /**
         * Invoked when a JMX notification occurs.
         * The implementation of this method should return as soon as possible, to avoid
         * blocking its notification broadcaster.
         *
         * @param notification The notification.
         * @param handback     An opaque object which helps the listener to associate
         *                     information regarding the MBean emitter. This object is passed to the
         *                     addNotificationListener call and resent, without modification, to the
         */
        @Override
        public void handleNotification(final Notification notification, final Object handback) {
            beginRead();
            try {
                if(notifications.containsKey(notification.getType()))
                    notifications.get(notification.getType()).handleNotification(notification, null);
            }
            finally {
                endRead();
            }
        }

        private Notification poll(final String resourceName){
            beginRead();
            try{
                for(final SshNotificationViewImpl notif: notifications.values())
                    if(notif.getEventName().startsWith(resourceName)){
                        final Notification result = notif.poll();
                        if(result != null) return result;
                    }
            }
            finally {
                endRead();
            }
            return null;
        }

        private Notification poll(){
            beginRead();
            try{
                for(final SshNotificationViewImpl notif: notifications.values()){
                    final Notification result = notif.poll();
                    if(result != null) return result;
                }
            }
            finally {
                endRead();
            }
            return null;
        }

        private Set<String> getNotifications(final String resourceName) {
            beginRead();
            try{
                final Set<String> result = Sets.newHashSetWithExpectedSize(notifications.size());
                for(final String listID: notifications.keySet())
                    if(listID.startsWith(resourceName))
                        result.add(listID);
                return result;
            }
            finally {
                endRead();
            }
        }

        private  <E extends Exception> boolean processNotification(final String notificationID, final Consumer<SshNotificationView, E> handler) throws E {
            final SshNotificationView notif;
            beginRead();
            try{
                notif = notifications.get(notificationID);
            }
            finally {
                endRead();
            }
            if(notif == null) return false;
            else {
                handler.accept(notif);
                return true;
            }
        }
    }

    private abstract static class AbstractSshAttributeView implements SshAttributeView {
        private final AttributeAccessor accessor;
        protected final Gson formatter;
        private final WellKnownType attributeType;

        private AbstractSshAttributeView(final AttributeAccessor accessor,
                                         final GsonBuilder builder){
            this.accessor = Objects.requireNonNull(accessor);
            this.formatter = builder.create();
            this.attributeType = accessor.getType();
        }

        @Override
        public final String getOriginalName() {
            return AttributeDescriptor.getAttributeName(accessor.getMetadata());
        }

        protected final <T> T getValue(final Class<T> attributeType) throws JMException {
            return accessor.getValue(attributeType);
        }

        private void printValueAsJson(final Writer output) throws IOException, JMException{
            formatter.toJson(accessor.getValue(), output);
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
            if(attributeType != null)
                accessor.setValue(formatter.fromJson(input, attributeType.getType()));
            else throw new UnsupportedOperationException(String.format("Attribute %s is read-only", getName()));
        }

        @Override
        public final void printOptions(final Writer output) throws IOException{
            final Descriptor descr = accessor.getMetadata().getDescriptor();
            for(final String fieldName: descr.getFieldNames())
                output.append(String.format("%s = %s", fieldName, descr.getFieldValue(fieldName)));
            output.flush();
        }

        @Override
        public final String getName() {
            return accessor.getName();
        }

        @Override
        public final boolean canRead() {
            return accessor.getMetadata().isReadable();
        }

        @Override
        public boolean canWrite() {
            return attributeType != null && accessor.getMetadata().isWritable();
        }
    }

    private static final class ReadOnlyAttributeView extends AbstractSshAttributeView{
        private ReadOnlyAttributeView(final AttributeAccessor accessor){
            super(accessor, new GsonBuilder());
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

    private static final class DefaultAttributeView extends AbstractSshAttributeView{

        private DefaultAttributeView(final AttributeAccessor accessor, final GsonBuilder builder) {
            super(accessor, builder);
        }

        @Override
        protected void printValueAsText(final Writer output) throws JMException, IOException {
            output.append(Objects.toString(getValue(Object.class), ""));
        }
    }

    private static abstract class AbstractBufferAttributeView<B extends Buffer> extends AbstractSshAttributeView{
        protected static final char WHITESPACE = ' ';
        private final Class<B> bufferType;

        private AbstractBufferAttributeView(final AttributeAccessor accessor,
                                    final AbstractBufferFormatter<B> formatter,
                                    final Class<B> bufferType){
            super(accessor, new GsonBuilder().registerTypeHierarchyAdapter(bufferType, formatter));
            this.bufferType = bufferType;
        }

        protected abstract void printValueAsText(final B buffer, final Writer output) throws IOException;

        @Override
        protected final void printValueAsText(final Writer output) throws JMException, IOException {
            printValueAsText(getValue(bufferType), output);
        }
    }

    private static final class ByteBufferAttributeView extends AbstractBufferAttributeView<ByteBuffer>{
        private ByteBufferAttributeView(final AttributeAccessor accessor){
            super(accessor, new ByteBufferFormatter(), ByteBuffer.class);
        }

        @Override
        protected void printValueAsText(final ByteBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining()) {
                output.append(Byte.toString(buffer.get()));
                output.append(WHITESPACE);
            }
        }
    }

    private static final class CharBufferAttributeView extends AbstractBufferAttributeView<CharBuffer>{
        private CharBufferAttributeView(final AttributeAccessor accessor){
            super(accessor, new CharBufferFormatter(), CharBuffer.class);
        }

        @Override
        protected void printValueAsText(final CharBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining()) {
                output.append(buffer.get());
                output.append(WHITESPACE);
            }
        }
    }

    private static final class ShortBufferAttributeView extends AbstractBufferAttributeView<ShortBuffer>{
        private ShortBufferAttributeView(final AttributeAccessor accessor){
            super(accessor, new ShortBufferFormatter(), ShortBuffer.class);
        }

        @Override
        protected void printValueAsText(final ShortBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining()) {
                output.append(Short.toString(buffer.get()));
                output.append(WHITESPACE);
            }
        }
    }

    private static final class IntBufferAttributeView extends AbstractBufferAttributeView<IntBuffer>{
        private IntBufferAttributeView(final AttributeAccessor accessor){
            super(accessor, new IntBufferFormatter(), IntBuffer.class);
        }

        @Override
        protected void printValueAsText(final IntBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining()) {
                output.append(Integer.toString(buffer.get()));
                output.append(WHITESPACE);
            }
        }
    }

    private static final class LongBufferAttributeView extends AbstractBufferAttributeView<LongBuffer>{
        private LongBufferAttributeView(final AttributeAccessor accessor){
            super(accessor, new LongBufferFormatter(), LongBuffer.class);
        }

        @Override
        protected void printValueAsText(final LongBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining()) {
                output.append(Long.toString(buffer.get()));
                output.append(WHITESPACE);
            }
        }
    }

    private static final class FloatBufferAttributeView extends AbstractBufferAttributeView<FloatBuffer>{
        private FloatBufferAttributeView(final AttributeAccessor accessor){
            super(accessor, new FloatBufferFormatter(), FloatBuffer.class);
        }

        @Override
        protected void printValueAsText(final FloatBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining()) {
                output.append(Float.toString(buffer.get()));
                output.append(WHITESPACE);
            }
        }
    }

    private static final class DoubleBufferAttributeView extends AbstractBufferAttributeView<DoubleBuffer>{
        private DoubleBufferAttributeView(final AttributeAccessor accessor){
            super(accessor, new DoubleBufferFormatter(), DoubleBuffer.class);
        }

        @Override
        protected void printValueAsText(final DoubleBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining()) {
                output.append(Double.toString(buffer.get()));
                output.append(WHITESPACE);
            }
        }
    }

    private static final class CompositeDataAttributeView extends AbstractSshAttributeView{
        private CompositeDataAttributeView(final AttributeAccessor accessor,
                                           final CompositeType type){
            super(accessor, new GsonBuilder()
                .registerTypeAdapter(ObjectName.class, new ObjectNameFormatter())
                .registerTypeHierarchyAdapter(CompositeData.class, new CompositeDataFormatter(type)));
        }

        @Override
        protected void printValueAsText(final Writer output) throws JMException, IOException {
            final CompositeData value = getValue(CompositeData.class);
            for(final String key: value.getCompositeType().keySet()){
                output.append(String.format("%s = %s", key, formatter.toJson(value.get(key))));
                output.append(System.lineSeparator());
            }
        }
    }

    private static final class TabularDataAttributeView extends AbstractSshAttributeView{
        private TabularDataAttributeView(final AttributeAccessor accessor,
                                         final TabularType type){
            super(accessor, new GsonBuilder()
                            .registerTypeHierarchyAdapter(TabularData.class, new TabularDataFormatter(type))
                            .registerTypeAdapter(ObjectName.class, new ObjectNameFormatter()));
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
                    final Collection<String> values = Collections2.transform(row.values(), new Function<Object, String>() {
                        @Override
                        public String apply(final Object cell) {
                            return formatter.toJson(cell);
                        }
                    });
                    output.append(joinString(values, ITEM_FORMAT, COLUMN_SEPARATOR));
                }
            });
        }
    }

    private static final class SshAttributesModel extends ThreadSafeObject implements AttributesModel{
        private final Map<String, AbstractSshAttributeView> attributes;

        private SshAttributesModel(){
            attributes = new HashMap<>(10);
        }

        private static String makeAttributeID(final String resourceName,
                                              final String userDefinedName){
            return resourceName + '/' + userDefinedName;
        }

        private static AbstractSshAttributeView createAttributeView(final AttributeAccessor accessor){
            final WellKnownType attributeType = accessor.getType();
            if(attributeType != null)
                switch (attributeType){
                    case BYTE_BUFFER:
                        return new ByteBufferAttributeView(accessor);
                    case CHAR_BUFFER:
                        return new CharBufferAttributeView(accessor);
                    case SHORT_BUFFER:
                        return new ShortBufferAttributeView(accessor);
                    case INT_BUFFER:
                        return new IntBufferAttributeView(accessor);
                    case LONG_BUFFER:
                        return new LongBufferAttributeView(accessor);
                    case FLOAT_BUFFER:
                        return new FloatBufferAttributeView(accessor);
                    case DOUBLE_BUFFER:
                        return new DoubleBufferAttributeView(accessor);
                    case DICTIONARY:
                        CompositeType compositeType = (CompositeType)accessor.getOpenType();
                        return new CompositeDataAttributeView(accessor, compositeType);
                    case DICTIONARY_ARRAY:
                        compositeType = (CompositeType)((ArrayType<?>)accessor.getOpenType()).getElementOpenType();
                        return new DefaultAttributeView(
                                accessor,
                                new GsonBuilder()
                                        .registerTypeHierarchyAdapter(CompositeData[].class, new ArrayOfCompositeDataFormatter(compositeType))
                        );
                    case TABLE:
                        TabularType tabularType = (TabularType)accessor.getOpenType();
                        return new TabularDataAttributeView(accessor, tabularType);
                    case TABLE_ARRAY:
                        tabularType = (TabularType)((ArrayType<?>)accessor.getOpenType()).getElementOpenType();
                        return new DefaultAttributeView(
                            accessor,
                            new GsonBuilder()
                                .registerTypeHierarchyAdapter(TabularData[].class, new ArrayOfTabularDataFormatter(tabularType))
                        );
                    case OBJECT_NAME:
                    case OBJECT_NAME_ARRAY:
                        return new DefaultAttributeView(accessor,
                                new GsonBuilder()
                                    .registerTypeAdapter(ObjectName.class, new ObjectNameFormatter())
                        );
                    default:
                        return new DefaultAttributeView(accessor, new GsonBuilder());
                }
            return new ReadOnlyAttributeView(accessor);
        }

        @Override
        public void addAttribute(final String resourceName,
                                 final String userDefinedName,
                                 final String attributeName,
                                 final AttributeConnector connector) {
            final String attributeID = makeAttributeID(resourceName, userDefinedName);
            beginWrite();
            try{
                if(attributes.containsKey(attributeID)) return;
                final AttributeAccessor accessor = connector.connect(attributeID);
                attributes.put(attributeID, createAttributeView(accessor));
            } catch (final JMException e) {
                SshHelpers.log(Level.SEVERE, "Unable to expose attribute %s", attributeID, e);
            } finally {
                endWrite();
            }
        }

        @Override
        public AttributeAccessor removeAttribute(final String resourceName,
                                                 final String userDefinedName,
                                                 final String attributeName) {
            final String attributeID = makeAttributeID(resourceName, userDefinedName);
            beginWrite();
            try{
                return attributes.containsKey(attributeID) ?
                        attributes.remove(attributeID).accessor:
                        null;
            }
            finally {
                endWrite();
            }
        }

        /**
         * Removes all attributes from this model.
         */
        @Override
        public void clear() {
            beginWrite();
            try{
                attributes.clear();
            }
            finally {
                endWrite();
            }
        }

        /**
         * Determines whether this model is empty.
         *
         * @return {@literal true}, if this model is empty; otherwise, {@literal false}.
         */
        @Override
        public boolean isEmpty() {
            beginRead();
            try {
                return attributes.isEmpty();
            }
            finally {
                endRead();
            }
        }

        private Set<String> getAttributes(final String resourceName) {
            beginRead();
            try{
                final Set<String> result = Sets.newHashSetWithExpectedSize(attributes.size());
                for(final String attributeID: attributes.keySet())
                    if(attributeID.startsWith(resourceName))
                        result.add(attributeID);
                return result;
            }
            finally {
                endRead();
            }
        }

        private  <E extends Exception> boolean processAttribute(final String attributeID, final Consumer<SshAttributeView, E> handler) throws E {
            final SshAttributeView attribute;
            beginRead();
            try {
                attribute = attributes.get(attributeID);
            }
            finally {
                endRead();
            }
            if(attribute == null) return false;
            else {
                handler.accept(attribute);
                return true;
            }
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
        populateModel(attributes);
        populateModel(notifications);
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
            clearModel(attributes);
            clearModel(notifications);
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
        return attributes.getAttributes(resourceName);
    }

    /**
     * Processes the attribute.
     *
     * @param attributeID ID of the attribute.
     * @param handler     The attribute processor.
     * @return {@literal true}, if attribute exists; otherwise, {@literal false}.
     * @throws E Unable to process attribute.
     */
    @Override
    public <E extends Exception> boolean processAttribute(final String attributeID, final Consumer<SshAttributeView, E> handler) throws E {
        return attributes.processAttribute(attributeID, handler);
    }

    @Override
    protected void resourceAdded(final String resourceName) {
        try {
            enlargeModel(resourceName, attributes);
            enlargeModel(resourceName, notifications);
        }
        catch (final Exception e){
            SshHelpers.log(Level.SEVERE, "Unable to process new resource %s. Restarting adapter %s. Context: %s",
                    resourceName,
                    NAME,
                    LogicalOperation.current(), e);
            super.resourceAdded(resourceName);
        }
    }

    @Override
    protected void resourceRemoved(final String resourceName) {
        clearModel(resourceName, attributes);
        clearModel(resourceName, notifications);
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

    @Override
    public <E extends Exception> boolean processNotification(final String notificationID, final Consumer<SshNotificationView, E> handler) throws E {
        return notifications.processNotification(notificationID, handler);
    }

    @Override
    public Notification poll(final String resourceName) {
        return notifications.poll(resourceName);
    }

    @Override
    public Notification poll() {
        return notifications.poll();
    }
}
