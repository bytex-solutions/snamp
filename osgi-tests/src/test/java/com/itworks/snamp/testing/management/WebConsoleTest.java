package com.itworks.snamp.testing.management;

import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.testing.AbstractSnampIntegrationTest;
import com.itworks.snamp.testing.SnampArtifact;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.junit.Test;
import org.ops4j.pax.exam.options.FrameworkPropertyOption;

import java.util.Arrays;
import java.util.Collection;

import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class WebConsoleTest extends AbstractSnampIntegrationTest {
    public WebConsoleTest(){
        super(mavenBundle("org.eclipse.jetty", "jetty-xml", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-security", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-io", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-http", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-util", "9.1.1.v20140108"),
                mavenBundle("javax.servlet", "javax.servlet-api", "3.1.0"),
                mavenBundle("org.eclipse.jetty", "jetty-server", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-webapp", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-servlet", "9.1.1.v20140108"),
                mavenBundle("org.eclipse.jetty", "jetty-servlet", "9.1.1.v20140108"),
                mavenBundle("com.sun.jersey", "jersey-core", "1.17.1"),
                mavenBundle("com.sun.jersey", "jersey-server", "1.17.1"),
                mavenBundle("com.sun.jersey", "jersey-servlet", "1.17.1"),
                mavenBundle("com.sun.jersey", "jersey-client", "1.17.1"),
                mavenBundle("com.google.code.gson", "gson", "2.2.4"),
                SnampArtifact.MANAGEMENT.getReference(),
                SnampArtifact.WEB_CONSOLE.getReference());
    }

    @Override
    protected Collection<FrameworkPropertyOption> getFrameworkProperties() {
        return Arrays.asList(
                frameworkProperty("com.itworks.snamp.webconsole.port").value("3344"),
                frameworkProperty("com.itworks.snamp.webconsole.host").value("localhost"));
    }

    /**
     * Creates a new configuration for running this test.
     *
     * @param config The configuration to set.
     */
    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {

    }

    @Test
    public void readConfigurationTest() throws InterruptedException {
        final Client webConsoleClient = new Client();
        final WebResource config = webConsoleClient.resource("http://127.0.0.1:3344/snamp/console/configuration");
        final String configJson = config.get(String.class);
    }
}
