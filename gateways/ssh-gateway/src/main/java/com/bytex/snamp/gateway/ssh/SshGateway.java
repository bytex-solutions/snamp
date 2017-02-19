package com.bytex.snamp.gateway.ssh;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationEventBox;
import com.bytex.snamp.gateway.modeling.*;
import com.bytex.snamp.jmx.ExpressionBasedDescriptorFilter;
import com.bytex.snamp.jmx.TabularDataUtils;
import com.bytex.snamp.jmx.WellKnownType;
import com.bytex.snamp.json.JsonUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.auth.CachingPublicKeyAuthenticator;
import org.apache.sshd.server.jaas.JaasPasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.codehaus.jackson.map.ObjectMapper;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.nio.*;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

/**
 * Represents SSH gateway.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SshGateway extends AbstractGateway implements GatewayController {

    private static final class SshModelOfNotifications extends ModelOfNotifications<SshNotificationAccessor>{
        private final NotificationEventBox mailbox;

        private SshModelOfNotifications(){
            mailbox = new NotificationEventBox(50);
        }

        @Override
        protected void cleared(){
            mailbox.clear();
        }

        @Override
        protected SshNotificationAccessor createAccessor(final String resourceName, final MBeanNotificationInfo metadata) {
            return new SshNotificationAccessor(metadata, mailbox, resourceName);
        }

        private Notification poll(final ExpressionBasedDescriptorFilter filter) {
            final NotificationEvent event = mailbox.poll();
            return filter == null || filter.match(event.getSource()) ?
                    event.getNotification() : null;
        }
    }

    private static final class ReadOnlyAttributeMapping extends SshAttributeAccessor {
        private ReadOnlyAttributeMapping(final MBeanAttributeInfo metadata, final ObjectMapper formatter){
            super(metadata, formatter);
        }

        @Override
        protected void printValueAsText(final Writer output) throws JMException, IOException {
            output.append(Objects.toString(getValue(), "NULL"));
        }

        @Override
        public boolean canWrite() {
            return false;
        }
    }

    private static final class DefaultAttributeMapping extends SshAttributeAccessor {

        private DefaultAttributeMapping(final MBeanAttributeInfo metadata, final ObjectMapper formatter) {
            super(metadata, formatter);
        }

        @Override
        protected void printValueAsText(final Writer output) throws JMException, IOException {
            output.append(Objects.toString(getValue(), ""));
        }
    }

    private static abstract class AbstractBufferAttributeMapping<B extends Buffer> extends SshAttributeAccessor {
        static final char WHITESPACE = ' ';
        private final Class<B> bufferType;

        private AbstractBufferAttributeMapping(final MBeanAttributeInfo metadata,
                                               final ObjectMapper formatter,
                                               final Class<B> bufferType){
            super(metadata, formatter);
            this.bufferType = bufferType;
        }

        protected abstract void printValueAsText(final B buffer, final Writer output) throws IOException;

        @Override
        protected final void printValueAsText(final Writer output) throws JMException, IOException {
            printValueAsText(getValue(bufferType), output);
        }
    }

    private static final class ByteBufferAttributeMapping extends AbstractBufferAttributeMapping<ByteBuffer> {
        private ByteBufferAttributeMapping(final MBeanAttributeInfo metadata, final ObjectMapper formatter){
            super(metadata, formatter, ByteBuffer.class);
        }

        @Override
        protected void printValueAsText(final ByteBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining())
                output.append(Byte.toString(buffer.get())).append(WHITESPACE);
        }
    }

    private static final class CharBufferAttributeMapping extends AbstractBufferAttributeMapping<CharBuffer> {
        private CharBufferAttributeMapping(final MBeanAttributeInfo metadata, final ObjectMapper formatter){
            super(metadata, formatter, CharBuffer.class);
        }

        @Override
        protected void printValueAsText(final CharBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining())
                output.append(buffer.get()).append(WHITESPACE);
        }
    }

    private static final class ShortBufferAttributeMapping extends AbstractBufferAttributeMapping<ShortBuffer> {
        private ShortBufferAttributeMapping(final MBeanAttributeInfo metadata, final ObjectMapper formatter){
            super(metadata, formatter, ShortBuffer.class);
        }

        @Override
        protected void printValueAsText(final ShortBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining())
                output.append(Short.toString(buffer.get())).append(WHITESPACE);
        }
    }

    private static final class IntBufferAttributeMapping extends AbstractBufferAttributeMapping<IntBuffer> {
        private IntBufferAttributeMapping(final MBeanAttributeInfo metadata, final ObjectMapper formatter){
            super(metadata, formatter, IntBuffer.class);
        }

        @Override
        protected void printValueAsText(final IntBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining())
                output.append(Integer.toString(buffer.get())).append(WHITESPACE);
        }
    }

    private static final class LongBufferAttributeMapping extends AbstractBufferAttributeMapping<LongBuffer> {
        private LongBufferAttributeMapping(final MBeanAttributeInfo metadata, final ObjectMapper formatter){
            super(metadata, formatter, LongBuffer.class);
        }

        @Override
        protected void printValueAsText(final LongBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining())
                output.append(Long.toString(buffer.get())).append(WHITESPACE);
        }
    }

    private static final class FloatBufferAttributeMapping extends AbstractBufferAttributeMapping<FloatBuffer> {
        private FloatBufferAttributeMapping(final MBeanAttributeInfo metadata, final ObjectMapper formatter){
            super(metadata, formatter, FloatBuffer.class);
        }

        @Override
        protected void printValueAsText(final FloatBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining())
                output.append(Float.toString(buffer.get())).append(WHITESPACE);
        }
    }

    private static final class DoubleBufferAttributeMapping extends AbstractBufferAttributeMapping<DoubleBuffer> {
        private DoubleBufferAttributeMapping(final MBeanAttributeInfo metadata, final ObjectMapper formatter){
            super(metadata, formatter, DoubleBuffer.class);
        }

        @Override
        protected void printValueAsText(final DoubleBuffer buffer, final Writer output) throws IOException {
            while (buffer.hasRemaining())
                output.append(Double.toString(buffer.get())).append(WHITESPACE);
        }
    }

    private static final class CompositeDataAttributeMapping extends SshAttributeAccessor {

        private CompositeDataAttributeMapping(final MBeanAttributeInfo metadata, final ObjectMapper formatter){
            super(metadata, formatter);
        }

        @Override
        protected void printValueAsText(final Writer output) throws JMException, IOException {
            final CompositeData value = getValue(CompositeData.class);
            for(final String key: value.getCompositeType().keySet())
                output
                        .append(String.format("%s = %s", key, formatter.writeValueAsString(value.get(key))))
                        .append(System.lineSeparator());
        }
    }

    private static final class TabularDataAttributeMapping extends SshAttributeAccessor {

        private TabularDataAttributeMapping(final MBeanAttributeInfo metadata, final ObjectMapper formatter) {
            super(metadata, formatter);
        }

        private static String joinString(final Stream<String> values,
                                         final String format,
                                         final String separator) {
            return String.join(separator, (CharSequence[]) values.map(input -> String.format(format, input)).toArray(String[]::new));
        }

        @Override
        protected void printValueAsText(final Writer output) throws JMException, IOException {
            final TabularData data = getValue(TabularData.class);
            final String COLUMN_SEPARATOR = "\t";
            final String ITEM_FORMAT = "%-10s";
            //print column first
            output.append(joinString(data.getTabularType().getRowType().keySet().stream(), ITEM_FORMAT, COLUMN_SEPARATOR));
            //print rows
            TabularDataUtils.forEachRow(data, row -> output.append(joinString(row.values().stream().map(obj -> {
                try {
                    return formatter.writeValueAsString(obj);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }), ITEM_FORMAT, COLUMN_SEPARATOR)));
        }
    }

    private static final class SshModelOfAttributes extends ModelOfAttributes<SshAttributeAccessor> {
        private final ObjectMapper formatter;

        private SshModelOfAttributes(final ObjectMapper formatter){
            this.formatter = Objects.requireNonNull(formatter);
        }

        @Override
        protected SshAttributeAccessor createAccessor(final String resourceName, final MBeanAttributeInfo metadata) {
            final WellKnownType attributeType = AttributeDescriptor.getType(metadata);
            if(attributeType != null)
                switch (attributeType){
                    case BYTE_BUFFER:
                        return new ByteBufferAttributeMapping(metadata, formatter);
                    case CHAR_BUFFER:
                        return new CharBufferAttributeMapping(metadata, formatter);
                    case SHORT_BUFFER:
                        return new ShortBufferAttributeMapping(metadata, formatter);
                    case INT_BUFFER:
                        return new IntBufferAttributeMapping(metadata, formatter);
                    case LONG_BUFFER:
                        return new LongBufferAttributeMapping(metadata, formatter);
                    case FLOAT_BUFFER:
                        return new FloatBufferAttributeMapping(metadata, formatter);
                    case DOUBLE_BUFFER:
                        return new DoubleBufferAttributeMapping(metadata, formatter);
                    case DICTIONARY:
                        return new CompositeDataAttributeMapping(metadata, formatter);
                    case TABLE:
                        return new TabularDataAttributeMapping(metadata, formatter);
                    default:
                        return new DefaultAttributeMapping(metadata, formatter);
                }
            return new ReadOnlyAttributeMapping(metadata, formatter);
        }
    }

    private static final class SshCommandFactory extends WeakReference<GatewayController> implements CommandFactory{
        private final ExecutorService commandExecutors;

        private SshCommandFactory(final GatewayController controller,
                                  final ExecutorService taskExecutor){
            super(controller);
            this.commandExecutors = Objects.requireNonNull(taskExecutor);
        }

        @Override
        public Command createCommand(final String commandLine) {
            final GatewayController controller = get();
            return controller != null && commandLine != null && commandLine.length() > 0 ?
                    ManagementShell.createSshCommand(commandLine, controller, commandExecutors) :
                    null;
        }
    }

    private SshServer server;
    private final SshModelOfAttributes attributes;
    private final SshModelOfNotifications notifications;
    private final ObjectMapper formatter;

    SshGateway(final String gatewayInstance) {
        super(gatewayInstance);
        notifications = new SshModelOfNotifications();
        formatter  = new ObjectMapper();
        formatter.registerModule(new JsonUtils());
        attributes = new SshModelOfAttributes(formatter);
    }

    private static void setupSecurity(final SshServer server, final SshSecuritySettings security) throws InvalidKeyException {
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

    private void start(final String host,
                       final int port,
                       final KeyPairProvider hostKeyFile,
                       final SshSecuritySettings security,
                       final ExecutorService threadPool) throws Exception{
        final SshServer server = SshServer.setUpDefaultServer();
        server.setHost(host);
        server.setPort(port);
        server.setKeyPairProvider(hostKeyFile);
        setupSecurity(server, security);
        server.setShellFactory(ManagementShell.createFactory(this, threadPool));
        server.setCommandFactory(new SshCommandFactory(this, threadPool));
        server.start();
        this.server = server;
    }

    @Override
    protected void start(final Map<String, String> parameters) throws Exception {
        final SshGatewayDescriptionProvider parser = SshGatewayDescriptionProvider.getInstance();
        start(parser.getHost(parameters),
                parser.getPort(parameters),
                parser.getKeyPairProvider(parameters),
                parser.getSecuritySettings(parameters),
                parser.getThreadPool(parameters));
    }

    /**
     * Stops the gateway.
     */
    @Override
    protected void stop() throws Exception{
        try {
            server.stop();
        }
        finally {
            attributes.clear();
            notifications.clear();
            server = null;
        }
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
                                                          final Acceptor<? super SshAttributeMapping, E> handler) throws E {
        return attributes.processAttribute(resourceName, attributeID, handler);
    }

    @Override
    public Notification poll(final ExpressionBasedDescriptorFilter filter) {
        return notifications.poll(filter);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>)attributes.addAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>)notifications.addNotification(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    @Override
    protected Stream<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) {
        return Stream.concat(
                attributes.clear(resourceName).stream(),
                notifications.clear(resourceName).stream()
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName, final M feature) {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>)attributes.removeAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>)notifications.removeNotification(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    @Override
    public void print(final Notification notif, final Writer output) {
        try {
            formatter.writeValue(output, notif);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Multimap<String, ? extends FeatureBindingInfo<MBeanAttributeInfo>> getAttributes(final AttributeSet<SshAttributeAccessor> attributes){
        final Multimap<String, ReadOnlyFeatureBindingInfo<MBeanAttributeInfo>> result = HashMultimap.create();
        attributes.forEachAttribute((resourceName, accessor) -> {
            final ImmutableMap.Builder<String, String> parameters = ImmutableMap.builder();
            if (accessor.canRead())
                parameters.put("read-command", accessor.getReadCommand(resourceName));
            if (accessor.canWrite())
                parameters.put("write-command", accessor.getWriteCommand(resourceName));
            return result.put(resourceName, new ReadOnlyFeatureBindingInfo<>(accessor, parameters.build()));
        });
        return result;
    }

    private static Multimap<String, ? extends FeatureBindingInfo<MBeanNotificationInfo>> getNotifications(final NotificationSet<SshNotificationAccessor> notifs){
        final Multimap<String, ReadOnlyFeatureBindingInfo<MBeanNotificationInfo>> result = HashMultimap.create();
        notifs.forEachNotification((resourceName, accessor) -> result.put(resourceName, new ReadOnlyFeatureBindingInfo<>(accessor, "listen-command", accessor.getListenCommand())));
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends MBeanFeatureInfo> Multimap<String, ? extends FeatureBindingInfo<M>> getBindings(final Class<M> featureType) {
        if (featureType.isAssignableFrom(MBeanAttributeInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>) getAttributes(attributes);
        else if (featureType.isAssignableFrom(MBeanNotificationInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>) getNotifications(notifications);
        return super.getBindings(featureType);
    }
}
