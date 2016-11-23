package com.bytex.snamp.testing.connector.zipkin;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.connector.zipkin.embedding.EmbeddedKafka;
import com.bytex.snamp.testing.ImportPackages;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connector.AbstractResourceConnectorTest;
import com.google.common.reflect.TypeToken;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Sender;
import zipkin.reporter.kafka08.KafkaSender;
import zipkin.reporter.urlconnection.URLConnectionSender;

import javax.management.JMException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@SnampDependencies({SnampFeature.ZIPKIN_CONNECTOR, SnampFeature.WRAPPED_LIBS})
@ImportPackages("zipkin.reporter.kafka08;version=\"[0.6.9,1)\"")
public class ZipkinConnectorTest extends AbstractResourceConnectorTest {
    public static final String CONNECTOR_TYPE = "zipkin";
    private EmbeddedKafka kafka;
    private final Path zookeeperDataDir;
    private final Path kafkaDir;

    public ZipkinConnectorTest() throws IOException {
        super(CONNECTOR_TYPE, "kafka://localhost:2181");
        zookeeperDataDir = Files.createTempDirectory("zookeeper");
        kafkaDir = Files.createTempDirectory("kafka");
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        final Properties zkProperties = new Properties(), kafkaProperties = new Properties();
        zkProperties.setProperty("dataDir", zookeeperDataDir.toString());
        zkProperties.setProperty("tickTime", "2000");
        zkProperties.setProperty("clientPort", "2181");
        kafkaProperties.setProperty("broker.id", "0");
        kafkaProperties.setProperty("port", "9092");
        kafkaProperties.setProperty("host.name", "localhost");
        kafkaProperties.setProperty("num.network.threads", "2");
        kafkaProperties.setProperty("num.io.threads", "4");
        kafkaProperties.setProperty("log.dirs", kafkaDir.toString());
        kafkaProperties.setProperty("num.partitions", "2");
        kafkaProperties.setProperty("zookeeper.connect", "localhost:2181");
        kafkaProperties.setProperty("zookeeper.connection.timeout.ms", "1000000");

        kafka = new EmbeddedKafka(kafkaProperties, zkProperties);
        kafka.start();
    }

    @Override
    protected void afterCleanupTest(BundleContext context) throws Exception {
        kafka.close();
        kafka = null;
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return true;
    }


    @Test
    public void kafkaTest() throws JMException, InterruptedException {
        final KafkaSender sender = KafkaSender.builder()
                .topic("zipkin")
                .bootstrapServers("localhost:9092")
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
