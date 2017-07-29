package com.bytex.snamp.testing.management;

import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.OperatingSystem;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.api.console.SessionFactory;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Shell commands test.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
@SnampDependencies({SnampFeature.JMX_CONNECTOR, SnampFeature.GROOVY_GATEWAY, SnampFeature.STANDARD_TOOLS})
public final class ShellManagementTest extends AbstractSnampIntegrationTest {

    private Object runCommand(String command) throws Exception {
        final ServiceHolder<SessionFactory> processorRef = ServiceHolder.tryCreate(getTestBundleContext(), SessionFactory.class)
                .orElseThrow(AssertionError::new);
        // On windows we have path separator that conflicts with escape symbols
        if (OperatingSystem.isWindows()) {
            command = command.replace("\\", "\\\\");
        }
        final Session session = processorRef.get().create(System.in, System.out, System.err);
        try {
            return session.execute(command);
        } finally {
            session.close();
            processorRef.release(getTestBundleContext());
        }
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws Exception {
        Thread.sleep(1000);
    }

    @Test
    public void threadPoolConfigTest() throws Exception{
        final Object result = runCommand("snamp:configure-thread-pool -m 3 -M 5 -t 2000 tp1");
        assertTrue(result instanceof CharSequence);
        Thread.sleep(1000); //adding thread pool is async operation
        final ServiceHolder<ThreadPoolRepository> threadPoolRepo = ServiceHolder.tryCreate(getTestBundleContext(), ThreadPoolRepository.class)
                .orElseThrow(AssertionError::new);
        final ServiceHolder<ConfigurationManager> configManager = ServiceHolder.tryCreate(getTestBundleContext(), ConfigurationManager.class)
                .orElseThrow(AssertionError::new);
        try{
            final ThreadPoolConfiguration config = configManager.get().transformConfiguration(cfg -> cfg.getThreadPools().get("tp1"));
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
            assertTrue(config.getGateways().containsKey("instance2"));
            assertEquals("dummy", config.getGateways().get("instance2").getType());
            assertEquals("v", config.getGateways().get("instance2").get("k"));
            return false;
        });
        runCommand("snamp:configure-gateway --delete instance2");
        Thread.sleep(500);
        processConfiguration(config -> {
            assertFalse(config.getGateways().containsKey("instance2"));
            return false;
        });
    }

    @Test
    public void startStopGatewayTest() throws Exception {
        runCommand("snamp:manage-gateway --enable=false groovy");
        runCommand("snamp:manage-gateway --enable=true groovy");
    }

    @Test
    public void startStopConnectorTest() throws Exception{
        runCommand("snamp:manage-connector --enable=false jmx");
        runCommand("snamp:manage-connector --enable=true jmx");
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
            assertTrue(config.getResources().containsKey("resource2"));
            assertEquals("dummy", config.getResources().get("resource2").getType());
            assertEquals("http://acme.com", config.getResources().get("resource2").getConnectionString());
            assertEquals("v", config.getResources().get("resource2").get("k"));
            return false;
        });

        //remove resource
        runCommand("snamp:configure-resource --delete resource2");
        Thread.sleep(500);
        processConfiguration(config -> {
            assertFalse(config.getResources().containsKey("resource2"));
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
        final String result = runCommand("snamp:resource -a -e resource1").toString();
        assertTrue(result.contains("Connection Type: dummyConnector"));
    }

    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {
        final GatewayConfiguration gatewayInstance =
                config.getGateways().getOrAdd("gatewayInst");
        gatewayInstance.setType("dummyGateway");
        final ManagedResourceConfiguration resource =
                config.getResources().getOrAdd("resource1");
        resource.setType("dummyConnector");
        resource.setConnectionString("http://acme.com");
    }
}
