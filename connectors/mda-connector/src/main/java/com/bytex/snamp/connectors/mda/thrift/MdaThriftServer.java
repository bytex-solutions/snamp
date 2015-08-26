package com.bytex.snamp.connectors.mda.thrift;

import com.bytex.snamp.SpecialUse;
import org.apache.thrift.TProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerTransport;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents Thrift server in which {@link TServer#serve()} method is non-blocking.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class MdaThriftServer extends TThreadPoolServer implements Runnable {
    private static final AtomicLong THREAD_COUNTER = new AtomicLong(0L);
    private final Thread serveThread;

    MdaThriftServer(final TServerTransport transport,
                    final ExecutorService threadPool,
                    final TProcessor processor){
        super(new Args(transport).executorService(threadPool).processor(processor));
        serveThread = new Thread(this, "ThriftProcessingThread#" + THREAD_COUNTER.getAndIncrement());
        serveThread.setDaemon(true);
        serveThread.setContextClassLoader(Thread.currentThread().getContextClassLoader());
    }

    @Override
    public void serve() {
        serveThread.start();
    }

    @Override
    @SpecialUse
    public void run() {
        super.serve();
    }

    @Override
    public void stop() {
        super.stop();
        try {
            serveThread.join(10000);
        } catch (final InterruptedException e) {
            serveThread.interrupt();
        }
    }
}
