package com.bytex.snamp.testing.management;

import com.bytex.snamp.SafeConsumer;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.OperatingSystem;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.junit.Test;

import java.io.*;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;

/**
 * Shell commands test.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.JMX_CONNECTOR, SnampFeature.GROOVY_ADAPTER})
public final class CommandsTest extends AbstractSnampIntegrationTest {
    private CharSequence runCommand(String command) throws Exception{
        final ServiceHolder<CommandProcessor> processorRef = new ServiceHolder<>(getTestBundleContext(), CommandProcessor.class);
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
            final CharSequence result = (CharSequence)session.execute(command);
            session.close();
            return result;
        } finally {
            processorRef.release(getTestBundleContext());
        }
    }

    @Test
    public void installedAdaptersTest() throws Exception {
        assertTrue(runCommand("snamp:installed-adapters").toString().startsWith("Groovy Resource Adapter"));
    }

    @Test
    public void installedConnectorsTest() throws Exception {
        final CharSequence result = runCommand("snamp:installed-connectors");
        assertTrue(result.toString().startsWith("JMX Connector"));
    }

    @Test
    public void installedComponentsTest() throws Exception{
        assertTrue(runCommand("snamp:installed-components").toString().contains(System.lineSeparator()));
    }

    @Test
    public void adapterInstancesTest() throws Exception{
        assertTrue(runCommand("snamp:adapter-instances").toString().startsWith("Instance: adapterInst"));
    }

    @Test
    public void adapterConfigurationTest() throws Exception{
        runCommand("snamp:configure-adapter -p k=v -p key=value instance2 dummy");
        //saving configuration is asynchronous process therefore it is necessary to wait
        Thread.sleep(500);
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                assertTrue(config.getResourceAdapters().containsKey("instance2"));
                assertEquals("dummy", config.getResourceAdapters().get("instance2").getAdapterName());
                assertEquals("v", config.getResourceAdapters().get("instance2").getParameters().get("k"));
            }
        }, true, false);
        runCommand("snamp:delete-adapter-param instance2 k");
        Thread.sleep(500);
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                assertTrue(config.getResourceAdapters().containsKey("instance2"));
                assertFalse(config.getResourceAdapters().get("instance2").getParameters().containsKey("k"));
            }
        }, true, false);
        runCommand("snamp:delete-adapter instance2");
        Thread.sleep(500);
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                assertFalse(config.getResourceAdapters().containsKey("instance2"));
            }
        }, true, false);
    }

    @Test
    public void startStopAdapterTest() throws Exception {
        runCommand("snamp:stop-adapter groovy");
        runCommand("snamp:start-adapter groovy");
    }

    @Test
    public void startStopConnectorTest() throws Exception{
        runCommand("snamp:stop-connector jmx");
        runCommand("snamp:start-connector jmx");
    }

    @Test
    public void jaasConfigTest() throws Exception{
        runCommand(String.format("snamp:setup-jaas %s", getPathToFileInProjectRoot("jaas.json")));
        final File jaasConfig = File.createTempFile("snamp", "jaas");
        runCommand(String.format("snamp:dump-jaas %s", jaasConfig.getPath()));
        try(final Reader reader = new FileReader(jaasConfig)){
            final Gson formatter = new Gson();
            final JsonElement element = formatter.fromJson(reader, JsonElement.class);
            assertNotNull(element);
        }
    }

    @Test
    public void resourcesTest() throws Exception{
        final String resources = runCommand("snamp:resources").toString();
        assertTrue(resources.startsWith("Resource: resource1. Type: dummyConnector. Connection string: http://acme.com"));
    }

    @Test
    public void configureResourceTest() throws Exception{
        runCommand("snamp:configure-resource -p k=v resource2 dummy http://acme.com");
        //saving configuration is asynchronous process therefore it is necessary to wait
        Thread.sleep(500);
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                assertTrue(config.getManagedResources().containsKey("resource2"));
                assertEquals("dummy", config.getManagedResources().get("resource2").getConnectionType());
                assertEquals("http://acme.com", config.getManagedResources().get("resource2").getConnectionString());
                assertEquals("v", config.getManagedResources().get("resource2").getParameters().get("k"));
            }
        }, true, false);

        //remove configuration parameter
        runCommand("snamp:delete-resource-param resource2 k");
        Thread.sleep(500);
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                assertTrue(config.getManagedResources().containsKey("resource2"));
                assertFalse(config.getManagedResources().get("resource2").getParameters().containsKey("k"));
            }
        }, true, false);
        //remove resource
        runCommand("snamp:delete-resource resource2");
        Thread.sleep(500);
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                assertFalse(config.getManagedResources().containsKey("resource2"));
            }
        }, true, false);
    }

    @Test
    public void configureAttributeTest() throws Exception{
        runCommand("snamp:configure-resource -p k=v resource2 dummy http://acme.com");
        //saving configuration is asynchronous process therefore it is necessary to wait
        Thread.sleep(500);
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                assertTrue(config.getManagedResources().containsKey("resource2"));
                assertEquals("dummy", config.getManagedResources().get("resource2").getConnectionType());
                assertEquals("http://acme.com", config.getManagedResources().get("resource2").getConnectionString());
                assertEquals("v", config.getManagedResources().get("resource2").getParameters().get("k"));
            }
        }, true, false);
        //register attribute
        runCommand("snamp:configure-attribute -p par=val resource2 attr 12000");
        Thread.sleep(500);
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                assertTrue(config.getManagedResources().containsKey("resource2"));
                final AttributeConfiguration attribute = config.getManagedResources()
                        .get("resource2")
                        .getFeatures(AttributeConfiguration.class)
                        .get("attr");
                assertNotNull(attribute);
                assertEquals(TimeSpan.ofSeconds(12), attribute.getReadWriteTimeout());
                assertEquals("val", attribute.getParameters().get("par"));
            }
        }, true, false);
        //remove configuration parameter
        runCommand("snamp:delete-attribute-param resource2 attr par");
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                assertTrue(config.getManagedResources().containsKey("resource2"));
                final AttributeConfiguration attribute = config.getManagedResources()
                        .get("resource2")
                        .getFeatures(AttributeConfiguration.class)
                        .get("attr");
                assertNotNull(attribute);
                assertFalse(attribute.getParameters().containsKey("par"));
            }
        }, true, false);
        //remove resource
        runCommand("snamp:delete-resource resource2");
        Thread.sleep(500);
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                assertFalse(config.getManagedResources().containsKey("resource2"));
            }
        }, true, false);
    }

    @Test
    public void configureEventTest() throws Exception{
        runCommand("snamp:configure-resource -p k=v resource2 dummy http://acme.com");
        //saving configuration is asynchronous process therefore it is necessary to wait
        Thread.sleep(500);
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                assertTrue(config.getManagedResources().containsKey("resource2"));
                assertEquals("dummy", config.getManagedResources().get("resource2").getConnectionType());
                assertEquals("http://acme.com", config.getManagedResources().get("resource2").getConnectionString());
                assertEquals("v", config.getManagedResources().get("resource2").getParameters().get("k"));
            }
        }, true, false);
        //register event
        runCommand("snamp:configure-event -p par=val resource2 ev1");
        Thread.sleep(500);
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                assertTrue(config.getManagedResources().containsKey("resource2"));
                final EventConfiguration attribute = config.getManagedResources()
                        .get("resource2")
                        .getFeatures(EventConfiguration.class)
                        .get("ev1");
                assertNotNull(attribute);
                assertEquals("val", attribute.getParameters().get("par"));
            }
        }, true, false);
        //remove configuration parameter
        runCommand("snamp:delete-event-param resource2 ev1 par");
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                assertTrue(config.getManagedResources().containsKey("resource2"));
                final EventConfiguration attribute = config.getManagedResources()
                        .get("resource2")
                        .getFeatures(EventConfiguration.class)
                        .get("ev1");
                assertNotNull(attribute);
                assertFalse(attribute.getParameters().containsKey("par"));
            }
        }, true, false);
        //remove resource
        runCommand("snamp:delete-resource resource2");
        Thread.sleep(500);
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                assertFalse(config.getManagedResources().containsKey("resource2"));
            }
        }, true, false);
    }

    @Test
    public void adapterInstanceInfoTest() throws Exception{
        final String result = runCommand("snamp:adapter adapterInst").toString();
        assertTrue(result.contains("System Name: dummyAdapter"));
    }

    @Test
    public void resourceInfoTest() throws Exception{
        //register event
        runCommand("snamp:configure-event -p par=val resource1 ev1");
        Thread.sleep(500);
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                assertTrue(config.getManagedResources().containsKey("resource1"));
                final EventConfiguration attribute = config.getManagedResources()
                        .get("resource1")
                        .getFeatures(EventConfiguration.class)
                        .get("ev1");
                assertNotNull(attribute);
                assertEquals("val", attribute.getParameters().get("par"));
            }
        }, true, false);
        Thread.sleep(500);
        //register attribute
        runCommand("snamp:configure-attribute -p par=val resource1 attr 12000");
        Thread.sleep(500);
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                assertTrue(config.getManagedResources().containsKey("resource1"));
                final AttributeConfiguration attribute = config.getManagedResources()
                        .get("resource1")
                        .getFeatures(AttributeConfiguration.class)
                        .get("attr");
                assertNotNull(attribute);
                assertEquals(TimeSpan.ofSeconds(12), attribute.getReadWriteTimeout());
                assertEquals("val", attribute.getParameters().get("par"));
            }
        }, true, false);
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
        final ResourceAdapterConfiguration adapter =
                config.getResourceAdapters().getOrAdd("adapterInst");
        adapter.setAdapterName("dummyAdapter");
        final ManagedResourceConfiguration resource =
                config.getManagedResources().getOrAdd("resource1");
        resource.setConnectionType("dummyConnector");
        resource.setConnectionString("http://acme.com");
    }
}