package com.bytex.snamp.testing.connectors.mda;

import com.bytex.snamp.connectors.ManagedResourceConnector;
import com.google.common.base.Supplier;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.junit.Test;

import javax.management.JMException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class StandaloneMdaConnectorTest extends AbstractMdaConnectorTest {
    public StandaloneMdaConnectorTest() {
        super(Collections.<String, String>emptyMap());
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return true;
    }

    @Test
    public void shortAttributeTest() throws IOException, JMException {
        JsonElement result = setAttributeViaHttp("short", new JsonPrimitive((short)52));
        assertEquals(0, result.getAsShort());
        result = getAttributeViaHttp("short");
        assertEquals(52, result.getAsShort());

        final ManagedResourceConnector connector = getManagementConnector();
        try {
            assertEquals((short)52, connector.getAttribute("attr1"));
            assertEquals((short)52, connector.getAttribute("alias"));
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes,
                                  final Supplier<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attr = attributeFactory.get();
        attr.setAttributeName("short");
        attr.getParameters().put("expectedType", "int16");
        attributes.put("attr1", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("short");
        attr.getParameters().put("expectedType", "int16");
        attributes.put("alias", attr);
    }
}
