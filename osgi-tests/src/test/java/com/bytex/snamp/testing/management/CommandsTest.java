package com.bytex.snamp.testing.management;

import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.core.PlatformVersion;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.OperatingSystem;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.junit.Test;

import java.io.*;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.EventConfiguration;

/**
 * Shell commands test.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.JMX_CONNECTOR, SnampFeature.GROOVY_GATEWAY})
public final class CommandsTest extends AbstractSnampIntegrationTest {
    private Object runCommand(String command) throws Exception{
        final ServiceHolder<CommandProcessor> processorRef = ServiceHolder.tryCreate(getTestBundleContext(), CommandProcessor.class);
        assertNotNull(processorRef);
        // On windows we have path separator that conflicts with escape symbols
        if (OperatingSystem.isWindows()) {
            command = command.replace("\\", "\\\\");
        }
        final File outFile = File.createTempFile("snamp", "out");
        final File errFile = File.createTempFile("snamp", "err");
        try (final PipedInputStream input = new PipedInputStream();
             final PipedOutputStream inputWriter = new PipedOutputStream(input);
             final OutputStream out = new FileOutputStream(outFile);
             final OutputStream err = new FileOutputStream(errFile)) {

            final CommandSession session = processorRef.getService().createSession(input, new PrintStream(out), new PrintStream(err));
            final Object result = session.execute(command);
            session.close();
            return result;
        } finally {
            processorRef.release(getTestBundleContext());
        }
    }

    @Test
    public void executeJavaScriptTest() throws Exception{
        Object result = runCommand("snamp:script \"snamp.version;\"");
        assertTrue(result instanceof PlatformVersion);
        result = runCommand("snamp:script \"var config; snamp.configure(function(conf) { config = conf; return false; }); config; \"");
        assertTrue(result instanceof AgentConfiguration);
    }

    @Test
    public void threadPoolConfigTest() throws Exception{
        final Object result = runCommand("snamp:thread-pool-add -m 3 -M 5 -t 2000 tp1");
        assertTrue(result instanceof CharSequence);
        Thread.sleep(1000); //adding thread pool is async operation
        final ServiceHolder<ThreadPoolRepository> threadPoolRepo = ServiceHolder.tryCreate(getTestBundleContext(), ThreadPoolRepository.class);
        final ServiceHolder<ConfigurationManager> configManager = ServiceHolder.tryCreate(getTestBundleContext(), ConfigurationManager.class);
        assertNotNull(threadPoolRepo);
        assertNotNull(configManager);
        try{
            final ThreadPoolConfiguration config = configManager.get().transformConfiguration(cfg -> cfg.getEntities(ThreadPoolConfiguration.class).get("tp1"));
            assertEquals(ThreadPoolConfiguration.INFINITE_QUEUE_SIZE, config.getQueueSize());
            assertEquals(3, config.getMinPoolSize());
            assertEquals(5, config.getMaxPoolSize());
            assertEquals(2000, config.getKeepAliveTime().toMillis());
            final ExecutorService executor = threadPoolRepo.get().getThreadPool("tp1", false);
            final Future<Integer> task = executor.submit(() -> 10);
            assertEquals(Integer.valueOf(10), task.get());
        }   finally {
            configManager.release(getTestBundleContext());
            threadPoolRepo.release(getTestBundleContext());
        }
    }

    @Test
    public void installedGatewaysTest() throws Exception {
        assertTrue(runCommand("snamp:installed-gateways").toString().startsWith("Groovy Gateway"));
    }

    @Test
    public void installedConnectorsTest() throws Exception {
        final CharSequence result = (CharSequence) runCommand("snamp:installed-connectors");
        assertTrue(result.toString().startsWith("JMX Connector"));
    }

    @Test
    public void installedComponentsTest() throws Exception{
        assertTrue(runCommand("snamp:installed-components").toString().contains(System.lineSeparator()));
    }

    @Test
    public void gatewayInstancesTest() throws Exception{
        assertTrue(runCommand("snamp:gateway-instances").toString().startsWith("Instance: gatewayInst"));
    }

    @Test
    public void gatewayConfigurationTest() throws Exception {
        runCommand("snamp:configure-gateway -p k=v -p key=value instance2 dummy");
        //saving configuration is asynchronous process therefore it is necessary to wait
        Thread.sleep(500);
        processConfiguration(config -> {
            assertTrue(config.getEntities(GatewayConfiguration.class).containsKey("instance2"));
            assertEquals("dummy", config.getEntities(GatewayConfiguration.class).get("instance2").getType());
            assertEquals("v", config.getEntities(GatewayConfiguration.class).get("instance2").getParameters().get("k"));
            return false;
        });
        runCommand("snamp:delete-gateway-param instance2 k");
        Thread.sleep(500);
        processConfiguration(config -> {
            assertTrue(config.getEntities(GatewayConfiguration.class).containsKey("instance2"));
            assertFalse(config.getEntities(GatewayConfiguration.class).get("instance2").getParameters().containsKey("k"));
            return false;
        });
        runCommand("snamp:delete-gateway instance2");
        Thread.sleep(500);
        processConfiguration(config -> {
            assertFalse(config.getEntities(GatewayConfiguration.class).containsKey("instance2"));
            return false;
        });
    }

    @Test
    public void startStopGatewayTest() throws Exception {
        runCommand("snamp:disable-gateway groovy");
        runCommand("snamp:enable-gateway groovy");
    }

    @Test
    public void startStopConnectorTest() throws Exception{
        runCommand("snamp:disable-connector jmx");
        runCommand("snamp:enable-connector jmx");
    }

    @Test
    public void resourcesTest() throws Exception{
        final String resources = runCommand("snamp:resources").toString();
        assertTrue(resources.startsWith("Resource: resource1. Type: dummyConnector. Connection string: http://acme.com"));
    }

    @Test
    public void configureResourceTest() throws Exception {
        runCommand("snamp:configure-resource -p k=v resource2 dummy http://acme.com");
        //saving configuration is asynchronous process therefore it is necessary to wait
        Thread.sleep(500);
        processConfiguration(config -> {
            assertTrue(config.getEntities(ManagedResourceConfiguration.class).containsKey("resource2"));
            assertEquals("dummy", config.getEntities(ManagedResourceConfiguration.class).get("resource2").getType());
            assertEquals("http://acme.com", config.getEntities(ManagedResourceConfiguration.class).get("resource2").getConnectionString());
            assertEquals("v", config.getEntities(ManagedResourceConfiguration.class).get("resource2").getParameters().get("k"));
            return false;
        });

        //remove configuration parameter
        runCommand("snamp:delete-resource-param resource2 k");
        Thread.sleep(500);
        processConfiguration(config -> {
            assertTrue(config.getEntities(ManagedResourceConfiguration.class).containsKey("resource2"));
            assertFalse(config.getEntities(ManagedResourceConfiguration.class).get("resource2").getParameters().containsKey("k"));
            return false;
        });
        //remove resource
        runCommand("snamp:delete-resource resource2");
        Thread.sleep(500);
        processConfiguration(config -> {
            assertFalse(config.getEntities(ManagedResourceConfiguration.class).containsKey("resource2"));
            return false;
        });
    }

    @Test
    public void configureAttributeTest() throws Exception {
        runCommand("snamp:configure-resource -p k=v resource2 dummy http://acme.com");
        //saving configuration is asynchronous process therefore it is necessary to wait
        Thread.sleep(500);
        processConfiguration(config -> {
            assertTrue(config.getEntities(ManagedResourceConfiguration.class).containsKey("resource2"));
            assertEquals("dummy", config.getEntities(ManagedResourceConfiguration.class).get("resource2").getType());
            assertEquals("http://acme.com", config.getEntities(ManagedResourceConfiguration.class).get("resource2").getConnectionString());
            assertEquals("v", config.getEntities(ManagedResourceConfiguration.class).get("resource2").getParameters().get("k"));
            return false;
        });
        //register attribute
        runCommand("snamp:configure-attribute -p par=val resource2 attr 12000");
        Thread.sleep(500);
        processConfiguration(config -> {
            assertTrue(config.getEntities(ManagedResourceConfiguration.class).containsKey("resource2"));
            final AttributeConfiguration attribute = config.getEntities(ManagedResourceConfiguration.class)
                    .get("resource2")
                    .getFeatures(AttributeConfiguration.class)
                    .get("attr");
            assertNotNull(attribute);
            assertEquals(12, attribute.getReadWriteTimeout(ChronoUnit.SECONDS));
            assertEquals("val", attribute.getParameters().get("par"));
            return false;
        });
        //remove configuration parameter
        runCommand("snamp:delete-attribute-param resource2 attr par");
        processConfiguration(config -> {
            assertTrue(config.getEntities(ManagedResourceConfiguration.class).containsKey("resource2"));
            final AttributeConfiguration attribute = config.getEntities(ManagedResourceConfiguration.class)
                    .get("resource2")
                    .getFeatures(AttributeConfiguration.class)
                    .get("attr");
            assertNotNull(attribute);
            assertFalse(attribute.getParameters().containsKey("par"));
            return false;
        });
        //remove resource
        runCommand("snamp:delete-resource resource2");
        Thread.sleep(500);
        processConfiguration(config -> {
            assertFalse(config.getEntities(ManagedResourceConfiguration.class).containsKey("resource2"));
            return false;
        });
    }

    @Test
    public void configureEventTest() throws Exception {
        runCommand("snamp:configure-resource -p k=v resource2 dummy http://acme.com");
        //saving configuration is asynchronous process therefore it is necessary to wait
        Thread.sleep(500);
        processConfiguration(config -> {
            assertTrue(config.getEntities(ManagedResourceConfiguration.class).containsKey("resource2"));
            assertEquals("dummy", config.getEntities(ManagedResourceConfiguration.class).get("resource2").getType());
            assertEquals("http://acme.com", config.getEntities(ManagedResourceConfiguration.class).get("resource2").getConnectionString());
            assertEquals("v", config.getEntities(ManagedResourceConfiguration.class).get("resource2").getParameters().get("k"));
            return false;
        });
        //register event
        runCommand("snamp:configure-event -p par=val resource2 ev1");
        Thread.sleep(500);
        processConfiguration(config -> {
            assertTrue(config.getEntities(ManagedResourceConfiguration.class).containsKey("resource2"));
            final EventConfiguration attribute = config.getEntities(ManagedResourceConfiguration.class)
                    .get("resource2")
                    .getFeatures(EventConfiguration.class)
                    .get("ev1");
            assertNotNull(attribute);
            assertEquals("val", attribute.getParameters().get("par"));
            return false;
        });
        //remove configuration parameter
        runCommand("snamp:delete-event-param resource2 ev1 par");
        processConfiguration(config -> {
            assertTrue(config.getEntities(ManagedResourceConfiguration.class).containsKey("resource2"));
            final EventConfiguration attribute = config.getEntities(ManagedResourceConfiguration.class)
                    .get("resource2")
                    .getFeatures(EventConfiguration.class)
                    .get("ev1");
            assertNotNull(attribute);
            assertFalse(attribute.getParameters().containsKey("par"));
            return false;
        });
        //remove resource
        runCommand("snamp:delete-resource resource2");
        Thread.sleep(500);
        processConfiguration(config -> {
            assertFalse(config.getEntities(ManagedResourceConfiguration.class).containsKey("resource2"));
            return false;
        });
    }

    @Test
    public void gatewayInstanceInfoTest() throws Exception {
        final String result = runCommand("snamp:gateway-instance gatewayInst").toString();
        assertTrue(result.contains("System Name: dummyGateway"));
    }

    @Test
    public void resourceInfoTest() throws Exception {
        //register event
        runCommand("snamp:configure-event -p par=val resource1 ev1");
        Thread.sleep(500);
        processConfiguration(config -> {
            assertTrue(config.getEntities(ManagedResourceConfiguration.class).containsKey("resource1"));
            final EventConfiguration attribute = config.getEntities(ManagedResourceConfiguration.class)
                    .get("resource1")
                    .getFeatures(EventConfiguration.class)
                    .get("ev1");
            assertNotNull(attribute);
            assertEquals("val", attribute.getParameters().get("par"));
            return false;
        });
        Thread.sleep(500);
        //register attribute
        runCommand("snamp:configure-attribute -p par=val resource1 attr 12000");
        Thread.sleep(500);
        processConfiguration(config -> {
            assertTrue(config.getEntities(ManagedResourceConfiguration.class).containsKey("resource1"));
            final AttributeConfiguration attribute = config.getEntities(ManagedResourceConfiguration.class)
                    .get("resource1")
                    .getFeatures(AttributeConfiguration.class)
                    .get("attr");
            assertNotNull(attribute);
            assertEquals(12, attribute.getReadWriteTimeout(ChronoUnit.SECONDS));
            assertEquals("val", attribute.getParameters().get("par"));
            return false;
        });
        Thread.sleep(500);
        //check resource
        final String resource = runCommand("snamp:resource -a -e resource1").toString();
        assertTrue(resource.contains("Connection String: http://acme.com"));
        assertTrue(resource.contains("attr"));
        assertTrue(resource.contains("ev1"));
        //cleanup resource
        runCommand("snamp:delete-attribute resource1 attr");
        runCommand("snamp:delete-event resource1 ev1");
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {
        final GatewayConfiguration gatewayInstance =
                config.getEntities(GatewayConfiguration.class).getOrAdd("gatewayInst");
        gatewayInstance.setType("dummyGateway");
        final ManagedResourceConfiguration resource =
                config.getEntities(ManagedResourceConfiguration.class).getOrAdd("resource1");
        resource.setType("dummyConnector");
        resource.setConnectionString("http://acme.com");
    }
}
