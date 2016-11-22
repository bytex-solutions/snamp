package com.bytex.snamp.testing.connector.zipkin;

import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.AbstractResourceConnectorTest;
import org.junit.Test;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.urlconnection.URLConnectionSender;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@SnampDependencies({SnampFeature.ZIPKIN_CONNECTOR, SnampFeature.WRAPPED_LIBS})
public class ZipkinConnectorTest extends AbstractResourceConnectorTest {
    public static final String CONNECTOR_TYPE = "zipkin";

    public ZipkinConnectorTest() {
        super(CONNECTOR_TYPE, "");
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return true;
    }

    @Test
    public void dummyTest() throws InterruptedException {
        final URLConnectionSender sender = URLConnectionSender.builder()
                .endpoint("http://localhost:8181/zipkin/api/v1/spans")
                .build();
        final AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build();
        final Span span = Span.builder()
                .duration(1000L)
                .id(123)
                .name("customSpan")
                .traceId(100500L)
                .build();
        reporter.report(span);
        assertNotNull(reporter);
        //Thread.sleep(300_000);
    }
}
