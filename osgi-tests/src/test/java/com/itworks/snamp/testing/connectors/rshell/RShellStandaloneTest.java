package com.itworks.snamp.testing.connectors.rshell;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import org.apache.commons.collections4.Factory;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Assume;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class RShellStandaloneTest extends AbstractRShellConnectorTest {
    private static final String USER_NAME = "Dummy";
    private static final String PASSWORD = "Password";
    private static final int PORT = 22000;
    private static final String FINGERPRINT = "e8:0d:af:84:bb:ec:05:03:b9:7c:f3:75:19:5a:2a:63";
    private static final String CERTIFICATE_FILE = "hostkey.ser";

    public RShellStandaloneTest() {
        super(USER_NAME,
                PASSWORD,
                PORT,
                CERTIFICATE_FILE,
                FINGERPRINT);
    }

    @Test
    public void readMemStatusAttribute() throws TimeoutException {
        Assume.assumeTrue(SystemUtils.IS_OS_LINUX);
        final ManagedResourceConnector<?> connector = getManagementConnector();
        assertNotNull(connector);
        try{
            final AttributeSupport attributes = connector.queryObject(AttributeSupport.class);
            assertNotNull(attributes);
            assertNotNull(attributes.connectAttribute("ms", "memStatus", new HashMap<String, String>(1) {{
                put("commandProfileLocation", "freemem-tool-profile.xml");
            }}));
            final Object table = attributes.getAttribute("ms", TimeSpan.INFINITE, null);
            assertNotNull(table);
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes, final Factory<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attr = attributeFactory.create();
        attr.setAttributeName("memStatus");
        attr.getParameters().put("commandProfileLocation", "freemem-tool-profile.xml");
        attributes.put("ms", attr);
    }
}
