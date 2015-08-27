package com.bytex.snamp.testing.connectors.mda;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.bytex.snamp.testing.connectors.mda.MonitoringDataAcceptor.Client;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class StandaloneMdaThriftConnectorTest extends AbstractMdaConnectorTest {
    public StandaloneMdaThriftConnectorTest(){
        super("thrift://localhost:9540", ImmutableMap.<String, String>of());
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return true;
    }

    private static Client createClient() throws IOException, TTransportException {
        final TTransport transport = new TSocket("localhost", 9540);
        transport.open();
        return new Client(new TBinaryProtocol(transport));
    }

    @Test
    public void shortAttributeTest() throws IOException, TException {
        final Client client = createClient();
        short result = client.set_short((short)50);
        assertEquals(0, result);
        assertEquals(50, client.get_short());
    }

    @Override
    protected void fillEvents(final Map<String, EventConfiguration> events, final Supplier<EventConfiguration> eventFactory) {
        EventConfiguration event = eventFactory.get();
        event.setCategory("testEvent1");
        event.getParameters().put("expectedType", "int64");
        events.put("e1", event);
    }

    @Override
    protected void fillAttributes(final Map<String, AttributeConfiguration> attributes,
                                  final Supplier<AttributeConfiguration> attributeFactory) {
        AttributeConfiguration attr = attributeFactory.get();
        attr.setAttributeName("short");
        attr.getParameters().put("expectedType", "int16");
        attributes.put("attr1", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("date");
        attr.getParameters().put("expectedType", "datetime");
        attributes.put("attr2", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("biginteger");
        attr.getParameters().put("expectedType", "bigint");
        attributes.put("attr3", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("str");
        attr.getParameters().put("expectedType", "string");
        attributes.put("attr4", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("array");
        attr.getParameters().put("expectedType", "array(int8)");
        attributes.put("attr5", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("boolean");
        attr.getParameters().put("expectedType", "bool");
        attributes.put("attr6", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("long");
        attr.getParameters().put("expectedType", "int64");
        attributes.put("attr7", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("dict");
        attr.getParameters().put("expectedType", "dictionary");
        attr.getParameters().put("dictionaryName", "MemoryStatus");
        attr.getParameters().put("dictionaryItemNames", "free, total");
        attr.getParameters().put("dictionaryItemTypes", "int32, int32");
        attributes.put("attr8", attr);
    }
}
