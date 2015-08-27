package com.bytex.snamp.connectors.mda.thrift;

import com.bytex.snamp.Box;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.AbstractManagedResourceConnector;
import com.bytex.snamp.connectors.ResourceEventListener;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.mda.DataAcceptor;
import com.bytex.snamp.connectors.mda.MDAAttributeSupport;
import com.bytex.snamp.connectors.mda.SimpleTimer;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.base.Supplier;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import javax.management.openmbean.*;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import static com.bytex.snamp.connectors.mda.MdaResourceConfigurationDescriptorProvider.parseType;

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

    private static final class ThriftAttributeSupport extends MDAAttributeSupport<ThriftAttributeAccessor> {
        private static final Class<ThriftAttributeAccessor> FEATURE_TYPE = ThriftAttributeAccessor.class;
        private final Cache<String, ThriftAttributeManager> parsers;

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
            ThriftAttributeManager parser = parsers.getIfPresent(descriptor.getAttributeName());
            if(parser != null){

            }
            else if(attributeType instanceof SimpleType<?> || attributeType instanceof ArrayType<?>){
                parser = new SimpleAttributeManager(WellKnownType.getType(attributeType), descriptor.getAttributeName());
                parser.saveTo(parsers);
            }
            else if(attributeType instanceof CompositeType){
                parser = new CompositeAttributeManager((CompositeType)attributeType, descriptor.getAttributeName());
                parser.saveTo(parsers);
            }
            else {
                parser = new FallbackAttributeManager(descriptor.getAttributeName());
                parser.saveTo(parsers);
            }
            final ThriftAttributeAccessor accessor = new ThriftAttributeAccessor(attributeID, attributeType, descriptor, parser);
            accessor.setValue(parser.getDefaultValue(), storage);
            return accessor;
        }

        private boolean getAttribute(final String attributeName, final TProtocol output) throws TException {
            final ThriftAttributeManager manager = parsers.getIfPresent(attributeName);
            if(manager == null)
                return false;
            else {
                manager.getValue(output, storage);
                return true;
            }
        }

        private boolean setAttribute(final String attributeName, final TProtocol input, final TProtocol output) throws TException {
            final ThriftAttributeManager manager = parsers.getIfPresent(attributeName);
            if(manager == null)
                return false;
            else {
                try {
                    manager.setValue(input, output, storage);
                } catch (final InvalidAttributeValueException e) {
                    throw new TException(e);
                }
                return true;
            }
        }
    }
    private final MdaThriftServer thriftServer;
    private final TServerTransport transport;
    private final ThriftAttributeSupport attributes;

    ThriftDataAcceptor(final String resourceName,
                       final long expirationTime,
                       final InetSocketAddress host,
                       final int socketTimeout,
                       final Supplier<? extends ExecutorService> threadPoolFactory) throws TTransportException {
        this.transport = new TNonblockingServerSocket(host, socketTimeout);
        this.thriftServer = new MdaThriftServer(this.transport, threadPoolFactory.get(), new WeakProcessor(this));
        final SimpleTimer lastWriteAccess = new SimpleTimer();
        this.attributes = new ThriftAttributeSupport(resourceName, expirationTime, lastWriteAccess, getLogger());
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
        return false;
    }

    @Override
    public void disableNotificationsExcept(final Set<String> notifications) {

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
        addResourceEventListener(listener, attributes);
    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {
        removeResourceEventListener(listener, attributes);
    }

    @Override
    public boolean process(final TProtocol in, final TProtocol out) throws TException {
        final Box<String> entityName = new Box<>();
        final MessageType messageType = MessageType.get(in.readMessageBegin(), entityName);
        if (messageType != null)
            try {
                switch (messageType) {
                    case GET_ATTRIBUTE:
                        return attributes.getAttribute(entityName.get(), out);
                    case SET_ATTRIBUTE:
                        return attributes.setAttribute(entityName.get(), in, out);
                    default:
                        return false;
                }
            } finally {
                in.readMessageEnd();
            }
        else return false;
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
        attributes.removeAll(true);
        attributes.parsers.invalidateAll();
    }
}
