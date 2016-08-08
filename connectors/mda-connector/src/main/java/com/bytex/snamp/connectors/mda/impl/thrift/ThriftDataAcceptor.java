package com.bytex.snamp.connectors.mda.impl.thrift;

import com.bytex.snamp.Box;
import com.bytex.snamp.connectors.mda.DataAcceptor;
import com.bytex.snamp.internal.Utils;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.*;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class ThriftDataAcceptor extends DataAcceptor implements TProcessor {
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

    private final MdaThriftServer thriftServer;
    private final TServerTransport transport;
    private final ThriftAttributeRepository attributes;
    private final ExecutorService threadPool;
    private final ThriftNotificationRepository notifications;

    ThriftDataAcceptor(final String resourceName,
                       final InetSocketAddress host,
                       final int socketTimeout,
                       final Supplier<? extends ExecutorService> threadPoolFactory) throws TTransportException {
        this.transport = new TServerSocket(host, socketTimeout);
        this.threadPool = threadPoolFactory.get();
        this.thriftServer = new MdaThriftServer(this.transport, threadPool, new WeakProcessor(this));
        this.attributes = new ThriftAttributeRepository(resourceName, getLogger());
        this.notifications = new ThriftNotificationRepository(resourceName,
                threadPool,
                Utils.getBundleContextOfObject(this),
                getLogger());
    }

    @Override
    @Aggregation(cached = true)
    protected ThriftAttributeRepository getAttributes() {
        return attributes;
    }

    @Override
    @Aggregation(cached = true)
    protected ThriftNotificationRepository getNotifications() {
        return notifications;
    }

    @Override
    public void beginListening(final Object... dependencies) {
        thriftServer.serve();
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
                    case RESET:
                        in.readStructBegin();
                        ThriftUtils.skipStopField(in);  //skip empty list of arguments
                        in.readStructEnd();
                        try {
                            attributes.reset();
                        } catch (final OpenDataException | InvalidAttributeValueException e) {
                            getLogger().log(Level.WARNING, "Unable to reset attributes", e);
                            return false;
                        }
                        return true;
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
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        thriftServer.stop();
        transport.close();
        attributes.close();
        notifications.close();
        super.close();
    }
}
