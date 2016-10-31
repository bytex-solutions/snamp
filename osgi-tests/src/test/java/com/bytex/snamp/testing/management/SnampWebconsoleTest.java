package com.bytex.snamp.testing.management;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;


/**
 * The Snamp webconsole test.
 * @author Evgeniy Kirichenko.
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.WEBCONSOLE)
public final class SnampWebconsoleTest extends AbstractSnampIntegrationTest {

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    //  @Test
    public void testBasicGet() throws IOException, InterruptedException {
        final URL attributeQuery = new URL("http://localhost:8181/snamp-webconsole/rest/get-data");
        // we should wait a while before it becomes reachable
        Thread.sleep(2000);
        //write attribute
        HttpURLConnection connection = (HttpURLConnection)attributeQuery.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        try{
            assertEquals(200, connection.getResponseCode());
            final String attributeValue = IOUtils.toString(connection.getInputStream(), Charset.defaultCharset());
            assertNotNull(attributeValue);
            assertFalse(attributeValue.isEmpty());
            assertEquals(attributeValue, "Yes, it works.");
        }
        finally {
            connection.disconnect();
        }
    }

    @Override
    protected void setupTestConfiguration(AgentConfiguration config) {

    }

    @Test
    public void dummyTest() throws InterruptedException {
        Thread.sleep(10000000);
    }
}