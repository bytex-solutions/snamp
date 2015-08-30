package com.bytex.snamp.connectors.mda.thrift;

import com.bytex.snamp.Box;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.AbstractManagedResourceConnector;
import com.bytex.snamp.connectors.ResourceEventListener;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.mda.DataAcceptor;
import com.bytex.snamp.connectors.mda.MDAAttributeSupport;
import com.bytex.snamp.connectors.mda.SimpleTimer;
import com.bytex.snamp.connectors.notifications.AbstractNotificationSupport;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.connectors.notifications.NotificationListenerInvoker;
import com.bytex.snamp.connectors.notifications.NotificationListenerInvokerFactory;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import javax.management.JMException;
import javax.management.openmbean.*;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.connectors.mda.MDAResourceConfigurationDescriptorProvider.parseType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ThriftDataAcceptor extends AbstractManagedResourceConnector implements DataAcceptor, TProcessor {
    private static final class WeakProcessor extends WeakReference<TProcessor> implements TProcessor{
        private WeakProcessor(final TProcessor value){
            super(value);
        }

        @Override
        public boolean process(final TProtocol in, final TProtocol out) throws TException {
            final TProcessor processor = get();
            return processor != null && processor.process(in, out);
        }
    }

    private static final class ThriftNotificationSupport extends AbstractNotificationSupport<ThriftNotificationAccessor>{
        private static final Class<ThriftNotificationAccessor> FEATURE_TYPE = ThriftNotificationAccessor.class;
        private final Logger logger;
        private final NotificationListenerInvoker listenerInvoker;
        private final SimpleTimer lastWriteAccess;

        private ThriftNotificationSupport(final String resourceName,
                                          final ExecutorService threadPool,
                                          final SimpleTimer lwa,
                                          final Logger logger){
            super(resourceName, FEATURE_TYPE);
            this.logger = Objects.requireNonNull(logger);
            this.lastWriteAccess = Objects.requireNonNull(lwa);
            this.listenerInvoker = NotificationListenerInvokerFactory.createParallelInvoker(threadPool);
        }

        @Override
        protected NotificationListenerInvoker getListenerInvoker() {
            return listenerInvoker;
        }

        @Override
        protected ThriftNotificationAccessor enableNotifications(final String notifType, final NotificationDescriptor metadata) throws OpenDataException{
            return new ThriftNotificationAccessor(notifType, metadata);
        }

        @Override
        protected void failedToEnableNotifications(final String listID, final String category, final Exception e) {
            failedToEnableNotifications(logger, Level.WARNING, listID, category, e);
        }

        private boolean fire(final String category, final TProtocol input) throws TException {
            fire(new NotificationCollector() {
                private Object userData = null;
                private long sequenceNumber = 0L;
                private long timeStamp = 0L;
                private String message = "";
                private boolean dataAvailable = false;

                private boolean readNotificationData(final ThriftNotificationAccessor metadata) throws TException{
                    input.readStructBegin();
                    int counter = 0;
                    while (true){
                        final TField field = input.readFieldBegin();
                        final boolean next;
                        if(next = field.type != TType.STOP)
                            switch (field.id) {
                                case 1:
                                    message = input.readString();
                                    counter += 1;
                                    break;
                                case 2:
                                    sequenceNumber = input.readI64();
                                    counter += 1;
                                    break;
                                case 3:
                                    timeStamp = input.readI64();
                                    counter += 1;
                                    break;
                                case 4:
                                    userData = metadata.parseUserData(input);
                                    counter += 1;
                                    break;
                                default:
                                    TProtocolUtil.skip(input, field.type);
                                    break;
                            }
                        input.readFieldEnd();
                        if(!next) break;
                    }
                    input.readStructEnd();
                    return counter == 4;
                }

                @Override
                protected void process(final ThriftNotificationAccessor metadata) {
                    if(category.equals(metadata.getDescriptor().getNotificationCategory())) {
                        if (!dataAvailable)
                            try {
                                dataAvailable = readNotificationData(metadata);
                            } catch (final TException e) {
                                logger.log(Level.SEVERE, "Unable to parse user data from notification " + sequenceNumber, e);
                            }
                        if (dataAvailable)
                            enqueue(metadata, message, sequenceNumber, timeStamp, userData);
                    }
                }
            });
            lastWriteAccess.reset();
            return true;
        }
    }

    private static final class ThriftAttributeSupport extends MDAAttributeSupport<ThriftAttributeAccessor> {
        private static final Class<ThriftAttributeAccessor> FEATURE_TYPE = ThriftAttributeAccessor.class;
        private final Cache<String, ThriftValueParser> parsers;

        private ThriftAttributeSupport(final String resourceName,
                                       final long expirationTime,
                                       final SimpleTimer lwa,
                                       final Logger logger){
            super(resourceName, FEATURE_TYPE, expirationTime, lwa, logger);
            this.parsers = CacheBuilder.newBuilder().weakValues().build();
        }

        @Override
        protected ThriftAttributeAccessor connectAttribute(final String attributeID,
                                                           final AttributeDescriptor descriptor) throws JMException{
            final OpenType<?> attributeType = parseType(descriptor);
            ThriftValueParser parser = parsers.getIfPresent(descriptor.getAttributeName());
            if(parser != null){

            }
            else if(attributeType instanceof SimpleType<?> || attributeType instanceof ArrayType<?>)
                ThriftAttributeAccessor.saveParser(parser = new SimpleValueParser(WellKnownType.getType(attributeType)),
                        descriptor,
                        parsers);
            else if(attributeType instanceof CompositeType)
                ThriftAttributeAccessor.saveParser(parser = new CompositeValueParser((CompositeType)attributeType),
                        descriptor,
                        parsers);
            else
                ThriftAttributeAccessor.saveParser(parser = FallbackValueParser.INSTANCE, descriptor, parsers);
            final ThriftAttributeAccessor accessor = new ThriftAttributeAccessor(attributeID, attributeType, descriptor);
            accessor.setValue(parser.getDefaultValue(), storage);
            return accessor;
        }

        private boolean getAttribute(final String attributeName, final TProtocol output) throws TException {
            final ThriftValueParser parser = parsers.getIfPresent(attributeName);
            if(parser == null)
                return false;
            else {
                ThriftAttributeAccessor.getValue(attributeName, storage, output, parser);
                return true;
            }
        }

        private boolean setAttribute(final String attributeName, final TProtocol input, final TProtocol output) throws TException {
            final ThriftValueParser parser = parsers.getIfPresent(attributeName);
            if (parser == null)
                return false;
            else {
                ThriftAttributeAccessor.setValue(attributeName, storage, input, output, parser);
                return true;
            }
        }

        @Override
        public void close() {
            super.close();
            parsers.invalidateAll();
            parsers.cleanUp();
        }
    }
    private final MdaThriftServer thriftServer;
    private final TServerTransport transport;
    private final ThriftAttributeSupport attributes;
    private final ExecutorService threadPool;
    private final ThriftNotificationSupport notifications;

    ThriftDataAcceptor(final String resourceName,
                       final long expirationTime,
                       final InetSocketAddress host,
                       final int socketTimeout,
                       final Supplier<? extends ExecutorService> threadPoolFactory) throws TTransportException {
        this.transport = new TServerSocket(host, socketTimeout);
        this.threadPool = threadPoolFactory.get();
        this.thriftServer = new MdaThriftServer(this.transport, threadPool, new WeakProcessor(this));
        final SimpleTimer lastWriteAccess = new SimpleTimer();
        this.attributes = new ThriftAttributeSupport(resourceName, expirationTime, lastWriteAccess, getLogger());
        this.notifications = new ThriftNotificationSupport(resourceName, threadPool, lastWriteAccess, getLogger());
    }

    @Override
    public boolean addAttribute(final String attributeID, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
        return attributes.addAttribute(attributeID, attributeName, readWriteTimeout, options) != null;
    }

    @Override
    public void removeAttributesExcept(final Set<String> attributes) {
        this.attributes.removeAllExcept(attributes);
    }

    @Override
    public boolean enableNotifications(final String listId, final String category, final CompositeData options) {
        return notifications.enableNotifications(listId, category, options) != null;
    }

    @Override
    public void disableNotificationsExcept(final Set<String> notifications) {
        this.notifications.removeAllExcept(notifications);
    }

    @Override
    public void beginAccept(final Object... dependencies) {
        thriftServer.serve();
    }

    /**
     * Adds a new listener for the connector-related events.
     * <p/>
     * The managed resource connector should holds a weak reference to all added event listeners.
     *
     * @param listener An event listener to add.
     */
    @Override
    public void addResourceEventListener(final ResourceEventListener listener) {
        addResourceEventListener(listener, attributes, notifications);
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes, notifications);
    }

    @Override
    public boolean process(final TProtocol in, final TProtocol out) throws TException {
        final Box<String> entityName = new Box<>();
        final TMessage message = in.readMessageBegin();
        final MessageType messageType = MessageType.get(message, entityName);
        if (messageType != null) {
            messageType.beginResponse(out, message.name, message.seqid);
            try {
                switch (messageType) {
                    case GET_ATTRIBUTE:
                        in.readStructBegin();
                        ThriftUtils.skipStopField(in);  //skip empty list of arguments
                        in.readStructEnd();
                        return attributes.getAttribute(entityName.get(), out);
                    case SET_ATTRIBUTE:
                        return attributes.setAttribute(entityName.get(), in, out);
                    case SEND_NOTIFICATION:
                        return notifications.fire(entityName.get(), in);
                    default:
                        return false;
                }
            } finally {
                in.readMessageEnd();
                messageType.endResponse(out);
            }
        }
        else return false;
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return findObject(objectType, new Function<Class<T>, T>() {
            @Override
            public T apply(final Class<T> objectType) {
                return ThriftDataAcceptor.super.queryObject(objectType);
            }
        }, attributes, notifications);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        super.close();
        thriftServer.stop();
        transport.close();
        threadPool.shutdown();
        attributes.close();
        notifications.removeAll(true, true);
    }
}
