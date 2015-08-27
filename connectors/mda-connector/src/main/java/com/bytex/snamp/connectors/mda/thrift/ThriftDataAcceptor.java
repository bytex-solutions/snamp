package com.bytex.snamp.connectors.mda.thrift;

import com.bytex.snamp.Box;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.AbstractManagedResourceConnector;
import com.bytex.snamp.connectors.ResourceEventListener;
import com.bytex.snamp.connectors.mda.DataAcceptor;
import com.google.common.base.Supplier;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import javax.management.openmbean.CompositeData;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ExecutorService;

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

    private static final class MDAAttributeSupport{

    }
    private final MdaThriftServer thriftServer;
    private final TServerTransport transport;

    ThriftDataAcceptor(final InetSocketAddress host,
                       final int socketTimeout,
                       final Supplier<? extends ExecutorService> threadPoolFactory) throws TTransportException {
        this.transport = new TNonblockingServerSocket(host, socketTimeout);
        this.thriftServer = new MdaThriftServer(this.transport, threadPoolFactory.get(), new WeakProcessor(this));
    }

    @Override
    public boolean addAttribute(final String attributeID, final String attributeName, final TimeSpan readWriteTimeout, final CompositeData options) {
        return false;
    }

    @Override
    public void removeAttributesExcept(final Set<String> attributes) {

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

    }

    /**
     * Removes connector event listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeResourceEventListener(final ResourceEventListener listener) {

    }

    @Override
    public boolean process(final TProtocol in, final TProtocol out) throws TException {
        final Box<String> entityName = new Box<>();
        final MessageType messageType = MessageType.get(in.readMessageBegin(), entityName);
        if (messageType != null)
            try {
                switch (messageType) {
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
    }
}
