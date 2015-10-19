package com.bytex.snamp.testing.management;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.junit.Test;

import java.io.*;

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
             final OutputStreamWriter inputWriter = new OutputStreamWriter(new PipedOutputStream(input));
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

    @Override
    protected boolean enableRemoteDebugging() {
        return true;
    }

    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {

    }
}
