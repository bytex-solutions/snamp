package com.bytex.snamp.testing.connector.zipkin;

import com.bytex.snamp.testing.ImportPackages;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.AbstractResourceConnectorTest;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Sender;
import zipkin.reporter.kafka08.KafkaSender;
import zipkin.reporter.urlconnection.URLConnectionSender;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@ImportPackages({"zipkin.reporter.kafka08;version=\"[0.6.9,1)\""})
@SnampDependencies({SnampFeature.ZIPKIN_CONNECTOR, SnampFeature.WRAPPED_LIBS})
public abstract class AbstractZipkinConnectorTest extends AbstractResourceConnectorTest {
    private static final String CONNECTOR_TYPE = "zipkin";

    protected AbstractZipkinConnectorTest(final String connectionString) {
        super(CONNECTOR_TYPE, connectionString);
    }

    protected AbstractZipkinConnectorTest(final String connectionString, final Map<String, String> parameters) {
        super(CONNECTOR_TYPE, connectionString, parameters);
    }

    protected static void sendSpans(final Sender sender, final Span... spans) {
        final AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build();
        for(final Span span: spans)
            reporter.report(span);
    }

    protected static Sender createHttpSender(final String url){
        return URLConnectionSender.builder()
                .endpoint(url)
                .build();
    }

    protected static Sender createHttpSender(){
        return createHttpSender("http://localhost:8181/zipkin/api/v1/spans");
    }

    protected static Sender createKafkaSender(final String topic, final String kafka){
        return KafkaSender.builder()
                .topic(topic)
                .bootstrapServers(kafka)
                .build();
    }
}
