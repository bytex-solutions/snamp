package com.bytex.snamp.testing.connector.zipkin;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.testing.ImportPackages;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.AbstractResourceConnectorTest;
import com.google.common.reflect.TypeToken;
import org.junit.Test;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Sender;
import zipkin.reporter.kafka08.KafkaSender;
import zipkin.reporter.urlconnection.URLConnectionSender;

import javax.management.JMException;
import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@SnampDependencies({SnampFeature.ZIPKIN_CONNECTOR, SnampFeature.WRAPPED_LIBS})
@ImportPackages("zipkin.reporter.kafka08;version=\"[0.6.9,1)\"")
public class ZipkinConnectorTest extends AbstractResourceConnectorTest {
    public static final String CONNECTOR_TYPE = "zipkin";

    public ZipkinConnectorTest() throws IOException {
        super(CONNECTOR_TYPE, "kafka://192.168.1.42:2181");
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return true;
    }


    @Test
    public void kafkaTest() throws JMException, InterruptedException {
        final KafkaSender sender = KafkaSender.builder()
                .topic("zipkin")
                .bootstrapServers("192.168.1.42:9092")
                .build();
        spanSendTest(sender);
    }

    @Test
    public void httpTest() throws InterruptedException, JMException {
        final URLConnectionSender sender = URLConnectionSender.builder()
                .endpoint("http://localhost:8181/zipkin/api/v1/spans")
                .build();
        spanSendTest(sender);
    }

    private void spanSendTest(final Sender sender) throws JMException, InterruptedException {
        final AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build();
        Span span = Span.builder()
                .duration(1000L)
                .id(123)
                .name("customSpan")
                .traceId(100500L)
                .build();
        reporter.report(span);
        span = Span.builder()
                .duration(1500L)
                .id(124)
                .name("customSpan")
                .traceId(100500L)
                .build();
        reporter.report(span);
        Thread.sleep(2000);
        testAttribute("summaryDuration", TypeToken.of(Double.class), 2500D / 1_000_000D, true);   //microseconds to seconds
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        attributes.addAndConsume("spans", attribute -> {
            attribute.setAlternativeName("customspan"); //Zipkin uses lower-cased span name
            attribute.getParameters().put("gauge", "timer");
        });
        attributes.addAndConsume("summaryDuration", attribute -> {
            attribute.getParameters().put("gauge", "get summaryValue from timer spans");
        });
    }
}
