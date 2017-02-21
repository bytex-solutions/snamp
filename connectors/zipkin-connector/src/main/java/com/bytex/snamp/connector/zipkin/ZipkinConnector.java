package com.bytex.snamp.connector.zipkin;

import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.dataStream.DataStreamConnector;
import com.bytex.snamp.connector.dataStream.groovy.GroovyNotificationParser;
import com.bytex.snamp.connector.dataStream.groovy.GroovyNotificationParserLoader;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.LoggerProvider;
import com.google.common.collect.ImmutableMap;
import groovy.lang.Binding;
import zipkin.collector.CollectorComponent;
import zipkin.storage.*;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.callUnchecked;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents Zipkin connector.
 */
final class ZipkinConnector extends DataStreamConnector implements AsyncSpanConsumer {

    //ephemeral exception indicating that the connector is closed
    private static final class ZipkinConnectorClosedException extends Exception{
        private static final long serialVersionUID = 8549679410018819272L;

        @Override
        public synchronized ZipkinConnectorClosedException fillInStackTrace() {
            return this;            
        }
    }

    private volatile Exception closeException;
    private CollectorComponent zipkinCollector;

    private ZipkinConnector(final String resourceName, final ManagedResourceInfo configuration, final ZipkinConnectorConfigurationDescriptionProvider provider) throws URISyntaxException {
        super(resourceName, configuration, provider);
        zipkinCollector = provider.createCollector(configuration.getConnectionString(), createStorage());
        if(zipkinCollector != null) {
            zipkinCollector.start();
            getLogger().info(String.format("Zipkin collector is started for resource %s", resourceName));
        } else {
            getLogger().info(String.format("Zipkin collector is not set for resource %s", resourceName));
        }
    }

    ZipkinConnector(final String resourceName, final ManagedResourceInfo configuration) throws URISyntaxException {
        this(resourceName, configuration, ZipkinConnectorConfigurationDescriptionProvider.getInstance());
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
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
                //only active cluster node can consume spans
                if (DistributedServices.isActiveNode(getBundleContextOfObject(ZipkinConnector.this)))
                    return ZipkinConnector.this;
                else
                    return (spans, callback) -> {
                    };  //NO OP consumer for passive cluster node
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
     * @return A new instance of notification parser.
     */
    @Override
    protected GroovyNotificationParser createNotificationParser() {
        return callUnchecked(() -> {
            final ZipkinConnectorConfigurationDescriptionProvider parametersParser = ZipkinConnectorConfigurationDescriptionProvider.getInstance();
            final URL[] scriptPath = parametersParser.parseScriptPath(getConfiguration());
            final boolean useServiceNameAsInstanceName = parametersParser.useServiceNameAsInstanceName(getConfiguration());
            final GroovyNotificationParserLoader loader = new GroovyNotificationParserLoader(this, getConfiguration(), true, scriptPath);
            final String scriptName = parametersParser.parseScriptFile(getConfiguration());
            final GroovyNotificationParser parser = loader.createScript(scriptName, new Binding());
            parser.setComponentName(getGroupName());
            parser.setInstanceName(getInstanceName());
            parser.setProperty("useServiceNameAsInstanceName", useServiceNameAsInstanceName);
            return parser;
        });
    }

    @Override
    public void accept(final List<zipkin.Span> spans, final Callback<Void> callback) {
        for (final zipkin.Span span : spans)
            try {
                dispatch(ImmutableMap.of(), span);
            } catch (final Exception e) {
                callback.onError(e);
                getLogger().log(Level.SEVERE, "Failed to dispatch span " + span, e);
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
            if (zipkinCollector != null)
                zipkinCollector.close();
            super.close();
        } catch (final Exception e) {
            closeException = e;
            throw e;
        } finally {
            zipkinCollector = null;
        }
        closeException = new ZipkinConnectorClosedException();
    }
}
