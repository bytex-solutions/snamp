package com.bytex.snamp.instrumentation.reporters.http;

import com.bytex.snamp.instrumentation.measurements.Measurement;
import com.bytex.snamp.instrumentation.reporters.Reporter;
import com.bytex.snamp.instrumentation.reporters.util.MeasurementBuffer;
import com.bytex.snamp.instrumentation.reporters.util.SoftMeasurementBuffer;
import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents HTTP reporter that can be worker in synchronous our asynchronous mode.
 * <p />
 *     This report can be used in conjunction with HTTP Connector.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class HttpReporter implements Reporter {
    public static final String TYPE = "http";

    private static final class SnampClientConfig extends DefaultClientConfig{
        private final Map<String, Object> properties;

        private SnampClientConfig(final Map<String, ?> properties){
            this.properties = properties == null ? new HashMap<String, Object>() : new HashMap<>(properties);
        }

        @Override
        public Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        public Object getProperty(final String propertyName) {
            return properties.get(propertyName);
        }
    }

    private static final class MeasurementSender extends WeakReference<HttpReporter> implements Runnable {
        private final Measurement[] measurements;

        private MeasurementSender(final HttpReporter reporter, final Measurement... measurements) {
            super(reporter);
            this.measurements = measurements;
        }

        @Override
        public void run() {
            final HttpReporter reporter = get();
            if (reporter != null)
                reporter.reportSync(measurements);
            clear();
        }
    }

    private static final Logger LOGGER = Logger.getLogger("SnampHttpReporter");

    /**
     * Represents configuration property of type {@link Boolean} indicating that HTTP reporter should compress outgoing measurements with GZIP.
     */
    public static final String GZIP_COMPRESSION_FEATURE = "com.bytex.snamp.reporter.http.compression";

    /**
     * Represents configuration property of type {@link java.util.concurrent.ExecutorService} used for asynchronous reporting.
     */
    public static final String EXECUTOR_SERVICE_PROPERTY = "com.bytex.snamp.reporter.http.threadPool";

    /**
     * Represents configuration property of type {@link MeasurementBuffer} used to save measurements when temporary network problem occurs.
     */
    public static final String BUFFER_PROPERTY = "com.bytex.snamp.reporter.http.buffer";

    private static final String BATCH_PATH = "/snamp/data/acquisition/measurements";
    private static final String NON_BATCH_PATH = "/snamp/data/acquisition/measurement";
    private final Client httpClient;
    private final WebResource batchResource;
    private final WebResource nonBatchResource;
    private final MeasurementBuffer buffer;
    private final AtomicBoolean resending;
    private boolean asynchronous;

    public HttpReporter(final URI snampLocation, final Map<String, ?> properties) {
        final DefaultClientConfig clientConfig = new SnampClientConfig(properties);
        clientConfig.getSingletons().add(new JacksonJsonProvider());

        httpClient = Client.create(clientConfig);
        if (clientConfig.getPropertyAsFeature(GZIP_COMPRESSION_FEATURE))
            httpClient.addFilter(new GZIPContentEncodingFilter());
        final ExecutorService customExecutorService = (ExecutorService) clientConfig.getProperty(EXECUTOR_SERVICE_PROPERTY);
        if(customExecutorService != null)
            httpClient.setExecutorService(customExecutorService);
        batchResource = httpClient.resource(UriBuilder.fromUri(snampLocation).path(BATCH_PATH).build());
        nonBatchResource = httpClient.resource(UriBuilder.fromUri(snampLocation).path(NON_BATCH_PATH).build());
        final MeasurementBuffer buffer = (MeasurementBuffer) clientConfig.getProperty(BUFFER_PROPERTY);
        this.buffer = buffer == null ? new SoftMeasurementBuffer() : buffer;
        resending = new AtomicBoolean(false);
        asynchronous = true;
    }

    public HttpReporter(final String snampLocation, final Map<String, ?> properties) throws URISyntaxException {
        this(new URI(snampLocation), properties);
    }

    public HttpReporter(final String snampLocation) throws URISyntaxException {
        this(snampLocation, Collections.<String, Object>emptyMap());
    }

    /**
     * Determines whether this sender is asynchronous.
     *
     * @return {@literal true} if this sender is asynchronous; otherwise, {@literal false}.
     */
    @Override
    public boolean isAsynchronous() {
        return asynchronous;
    }

    public void setAsynchronous(final boolean value){
        asynchronous = value;
    }

    /**
     * Determines whether this reporter is connected to the SNAMP server.
     *
     * @return {@literal true}, if this reporter is connected to the server; otherwise, {@literal false}.
     */
    @Override
    public boolean isConnected() {
        return true;
    }

    /**
     * Flushes buffered measurements.
     *
     * @throws IOException HTTP error occurred when posting measurements to SNAMP.
     */
    @Override
    public void flush() throws IOException {
        try {
            resendMeasurements();
        } catch (final ClientHandlerException e){
            throw new IOException(e);
        }
    }

    private void resendMeasurements() throws ClientHandlerException {
        Measurement measurement;
        while ((measurement = buffer.remove()) != null)
            try {
                nonBatchResource.type(MediaType.APPLICATION_JSON_TYPE).post(measurement);
            } catch (final ClientHandlerException e){
                //save the measurement again and abort sending
                saveMeasurements(measurement);
                throw e;
            }
    }

    private void saveMeasurements(final Measurement... measurements) {
        for (final Measurement measurement : measurements)
            switch (buffer.place(measurement)) {
                case NOT_ENOUGH_SPACE:
                    LOGGER.warning(String.format("Measurement %s was dropped", measurement));
                    continue;
                case DROP_OLD_MEASUREMENT:
                    LOGGER.warning("One of buffered measurements was dropped");
            }
    }

    private void reportSync(final Measurement[] measurements) {
        final ClientRequest request = ClientRequest.create()
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(measurements)
                .build(batchResource.getURI(), HttpMethod.POST);
        final ClientResponse response;
        try {
            response = batchResource.getHeadHandler().handle(request);
        } catch (final ClientHandlerException e) {
            LOGGER.log(Level.WARNING, String.format("Unable to send measurements to %s", batchResource.getURI()), e);
            saveMeasurements(measurements);
            return;
        }
        switch (response.getStatus()) {
            case 204:
            case 200:
                LOGGER.fine("Successfully submitted measurements");
                //only one thread can be used for resending measurements at a time
                if (resending.compareAndSet(false, true))
                    try {
                        resendMeasurements();
                    } finally {
                        resending.set(false);
                    }
                return;
            default:
                LOGGER.warning(String.format("Failed to submit measurements. Response code is %s (%s)", response.getStatus(), response.getStatusInfo().getReasonPhrase()));
                saveMeasurements(measurements);
        }
    }

    /**
     * Send one or more measurements.
     *
     * @param measurements A set of measurements to send.
     */
    @Override
    public void report(final Measurement... measurements) {
        //send this portion of measurements
        if (asynchronous)
            httpClient.getExecutorService().execute(new MeasurementSender(this, measurements));
        else
            reportSync(measurements);
    }

    public int getBufferedMeasurements(){
        return buffer.size();
    }

    @Override
    public void close() throws IOException {
        httpClient.destroy();
        buffer.clear();
    }
}
