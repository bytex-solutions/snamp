package com.bytex.snamp.testing.connector.zipkin;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.connector.zipkin.embedding.EmbeddedKafka;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import zipkin.Span;
import zipkin.reporter.Sender;

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
public class ZipkinConnectorTest extends AbstractZipkinConnectorTest {
    private EmbeddedKafka kafka;
    private final Path zookeeperDataDir;
    private final Path kafkaDir;

    public ZipkinConnectorTest() throws IOException {
        super("kafka://localhost:2181");
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
        return false;
    }


    @Test
    public void kafkaTest() throws JMException, InterruptedException {
        spanSendTest(createKafkaSender("zipkin", "localhost:9092"));
    }

    @Test
    public void httpTest() throws InterruptedException, JMException {
        spanSendTest(createHttpSender());
    }

    private void spanSendTest(final Sender sender) throws JMException, InterruptedException {
        final Span span1 = Span.builder()
                .duration(1000L)
                .id(123)
                .name("customSpan")
                .traceId(100500L)
                .timestamp(System.currentTimeMillis())
                .build();
        final Span span2 = Span.builder()
                .duration(1500L)
                .id(124)
                .name("customSpan")
                .timestamp(System.currentTimeMillis())
                .traceId(100500L)
                .build();
        sendSpans(sender, span1, span2);
        Thread.sleep(5_000);
        testAttribute("summaryDuration", TypeToken.of(Double.class), 2_500D / 1_000_000D, true);   //microseconds to seconds
    }

    @Test
    public void configurationTest(){
        testConfigurationDescriptor(ManagedResourceConfiguration.class, ImmutableSet.of(
                "instanceName",
                "componentName",
                "synchronizationPeriod",
                "parserScriptPath",
                "parserScript"
        ));
        testConfigurationDescriptor(AttributeConfiguration.class, ImmutableSet.of(
                "from",
                "to",
                "filter",
                "gauge"
        ));
        testConfigurationDescriptor(EventConfiguration.class, ImmutableSet.of(
                "filter"
        ));
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        attributes.addAndConsume("spans", attribute -> {
            attribute.setAlternativeName("customspan"); //Zipkin uses lower-cased span name
            attribute.put("gauge", "timer");
        });
        attributes.addAndConsume("summaryDuration", attribute -> {
            attribute.put("gauge", "get summaryValue from timer spans");
        });
    }
}
