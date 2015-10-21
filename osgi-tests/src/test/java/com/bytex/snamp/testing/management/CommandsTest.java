package com.bytex.snamp.testing.management;

import com.bytex.snamp.SafeConsumer;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.junit.Test;

import java.io.*;

import static com.bytex.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;

/**
 * Shell commands test.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies({SnampFeature.JMX_CONNECTOR, SnampFeature.GROOVY_ADAPTER})
public final class CommandsTest extends AbstractSnampIntegrationTest {
    private CharSequence runCommand(final CharSequence command) throws Exception{
        final ServiceHolder<CommandProcessor> processorRef = new ServiceHolder<>(getTestBundleContext(), CommandProcessor.class);
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
        assertTrue(runCommand("snamp:installed-adapters").toString().startsWith("groovy"));
    }

    @Test
    public void installedConnectorsTest() throws Exception {
        assertTrue(runCommand("snamp:installed-connectors").toString().startsWith("jmx"));
    }

    @Test
    public void installedComponentsTest() throws Exception{
        assertTrue(runCommand("snamp:installed-components").toString().contains(System.lineSeparator()));
    }

    @Test
    public void adapterInstancesTest() throws Exception{
        assertTrue(runCommand("snamp:configured-adapters").toString().startsWith("Instance: adapterInst"));
    }

    @Test
    public void configureAdapterTest() throws Exception{
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
        runCommand("snamp:delete-adapter instance2");
        Thread.sleep(500);
        processConfiguration(new SafeConsumer<AgentConfiguration>() {
            @Override
            public void accept(final AgentConfiguration config) {
                assertFalse(config.getResourceAdapters().containsKey("instance2"));
            }
        }, true, false);
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return true;
    }

    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {
        final ResourceAdapterConfiguration adapter =
                config.newConfigurationEntity(ResourceAdapterConfiguration.class);
        adapter.setAdapterName("dummyAdapter");
        config.getResourceAdapters().put("adapterInst", adapter);
    }
}
