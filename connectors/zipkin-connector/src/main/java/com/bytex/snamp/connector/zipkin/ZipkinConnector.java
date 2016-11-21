package com.bytex.snamp.connector.zipkin;

import com.bytex.snamp.connector.md.MessageDrivenConnector;
import com.bytex.snamp.connector.md.NotificationParser;
import com.bytex.snamp.connector.md.notifications.NotificationSource;
import com.bytex.snamp.instrumentation.Identifier;
import com.bytex.snamp.instrumentation.Span;
import com.bytex.snamp.io.Buffers;
import com.google.common.collect.ImmutableMap;
import zipkin.collector.CollectorComponent;
import zipkin.storage.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/**
 * Represents Zipkin connector.
 */
final class ZipkinConnector extends MessageDrivenConnector implements AsyncSpanConsumer {
    //ephemeral exception indicating that the connector is closed
    private static final class ZipkinConnectorClosedException extends Exception{
        private static final long serialVersionUID = 8549679410018819272L;

        @Override
        public synchronized ZipkinConnectorClosedException fillInStackTrace() {
            return this;
        }
    }

    private final CollectorComponent zipkinCollector;

    private ZipkinConnector(final String resourceName, final Map<String, String> parameters, final ZipkinConnectorConfigurationDescriptionProvider provider){
        super(resourceName, parameters, provider);
        zipkinCollector = provider.createCollector(createStorage());
        assert zipkinCollector != null;
        zipkinCollector.start();
    }

    private volatile Exception closeException;

    ZipkinConnector(final String resourceName, final Map<String, String> parameters) {
        this(resourceName, parameters, ZipkinConnectorConfigurationDescriptionProvider.getInstance());
    }

    private StorageComponent createStorage(){
        return new StorageComponent() {
            @Override
            public SpanStore spanStore() {
                throw new UnsupportedOperationException();
            }

            @Override
            public AsyncSpanStore asyncSpanStore() {
                throw new UnsupportedOperationException();
            }

            @Override
            public AsyncSpanConsumer asyncSpanConsumer() {
                return ZipkinConnector.this;
            }

            @Override
            public CheckResult check() {
                final Exception error = ZipkinConnector.this.closeException;
                return error == null ? CheckResult.OK : CheckResult.failed(error);
            }

            @Override
            public void close() {
            }
        };
    }

    /**
     * Creates a new notification parser.
     *
     * @param resourceName Resource name.
     * @param source       Component identity.
     * @param parameters   Set of parameters that may be used by notification parser.
     * @return A new instance of notification parser.
     */
    @Override
    protected NotificationParser createNotificationParser(final String resourceName, final NotificationSource source, final Map<String, String> parameters) {
        return null;
    }

    private static Span toNativeSpan(final zipkin.Span zipkinSpan){
        final Span result = new Span();
        result.setName(zipkinSpan.name);
        result.setSpanID(Identifier.ofLong(zipkinSpan.id));
        if(zipkinSpan.parentId != null)
            result.setParentSpanID(Identifier.ofLong(zipkinSpan.parentId));
        if(zipkinSpan.traceIdHigh == 0)
            result.setCorrelationID(Identifier.ofLong(zipkinSpan.traceId));
        else {
            final ByteBuffer traceId128 = Buffers.allocByteBuffer(16, false);
            traceId128.putLong(zipkinSpan.traceIdHigh);
            traceId128.putLong(zipkinSpan.traceId);
            result.setCorrelationID(Identifier.ofBytes(traceId128.array()));
        }
        return result;
    }

    @Override
    public void accept(final List<zipkin.Span> spans, final Callback<Void> callback) {
        for (final zipkin.Span span : spans)
            try {
                dispatch(ImmutableMap.of(), span);
            } catch (final Exception e) {
                callback.onError(e);
                return;
            }
        callback.onSuccess(null);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resource clearly.
     */
    @Override
    public void close() throws Exception {
        try {
            zipkinCollector.close();
            super.close();
        } catch (final Exception e) {
            closeException = e;
            throw e;
        }
        closeException = new ZipkinConnectorClosedException();
    }
}
