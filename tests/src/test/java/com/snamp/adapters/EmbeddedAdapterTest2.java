package com.snamp.adapters;


import com.snamp.connectors.RmiConnectorTest;
import com.snamp.hosting.AgentConfiguration;
import org.junit.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class EmbeddedAdapterTest2 extends RmiConnectorTest<RemoteBean> {
    private static final String BINDING_NAME = "remote-bean";

    public EmbeddedAdapterTest2(){
        super(TestEmbeddedAdapter.NAME, new HashMap<String, String>(), new RemoteBean(), BINDING_NAME);
    }

    @Test
    public final void testRmiBigintProperty(){
        final TestEmbeddedAdapter adapter = getTestContext().queryObject(TestEmbeddedAdapter.class);
        adapter.setBigIntProperty(BigInteger.valueOf(42L));
        assertEquals(BigInteger.valueOf(42L), adapter.getBigIntProperty());
    }

    @Override
    protected void fillAttributes(final Map<String, ManagementTargetConfiguration.AttributeConfiguration> attributes) {
        TestEmbeddedAdapter.fillAttributes(attributes);
    }
}
