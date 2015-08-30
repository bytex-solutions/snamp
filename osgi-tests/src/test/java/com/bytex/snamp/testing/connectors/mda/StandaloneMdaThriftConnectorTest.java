package com.bytex.snamp.testing.connectors.mda;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.concurrent.SynchronizationEvent;
import com.bytex.snamp.connectors.notifications.NotificationSupport;
import com.bytex.snamp.connectors.notifications.SynchronizationListener;
import com.bytex.snamp.io.Buffers;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.Test;

import javax.management.Notification;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.TimeoutException;

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
        super("thrift://localhost:9540", ImmutableMap.of("socketTimeout", "10000"));
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return true;
    }

    private static Client createClient() throws IOException, TTransportException {
        final TTransport transport = new TSocket("localhost", 9540, 10000);
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

    @Test
    public void dateAttributeTest() throws IOException, TException{
        final Client client = createClient();
        final long date = System.currentTimeMillis();
        final long result = client.set_date(date);
        assertEquals(0L, result);
        assertEquals(date, client.get_date());
    }

    @Test
    public void bigintAttributeTest() throws IOException, TException{
        final Client client = createClient();
        final BigInteger expectedValue = new BigInteger("100500");
        final BigInteger result = new BigInteger(client.set_biginteger(expectedValue.toString()));
        assertEquals(BigInteger.ZERO, result);
        assertEquals(expectedValue, new BigInteger(client.get_biginteger()));
    }

    @Test
    public void strAttributeTest() throws IOException, TException{
        final Client client = createClient();
        final String expectedValue = "Frank Underwood";
        final String result = client.set_str(expectedValue);
        assertEquals("", result);
        assertEquals(expectedValue, client.get_str());
    }

    @Test
    public void arrayAttributeTest() throws IOException, TException {
        final Client client = createClient();
        final byte[] expectedValue = {3, 90, 50, 7};
        final ByteBuffer result = client.set_array(Buffers.wrap(expectedValue));
        assertArrayEquals(ArrayUtils.emptyArray(byte[].class), Buffers.readRemaining(result));
        assertArrayEquals(expectedValue, Buffers.readRemaining(client.get_array()));
    }

    @Test
    public void booleanAttributeTest() throws IOException, TException{
        final Client client = createClient();
        final boolean result = client.set_boolean(true);
        assertFalse(result);
        assertTrue(client.get_boolean());
    }

    @Test
    public void longAttributeTest() throws IOException, TException {
        final Client client = createClient();
        final long expectedValue = 42L;
        final long result = client.set_long(expectedValue);
        assertEquals(0L, result);
        assertEquals(expectedValue, client.get_long());
        assertEquals(expectedValue, client.get_long());
    }

    @Test
    public void longReadTest() throws IOException, TException, InterruptedException {
        final Client client = createClient();
        assertEquals(0L, client.get_long());
        Thread.sleep(1000);
        assertEquals(0L, client.get_long());
    }

    @Test
    public void dictAttributeTest() throws IOException, TException{
        final Client client = createClient();
        final MemoryStatus expectedValue = new MemoryStatus(32, 100L);
        final MemoryStatus result = client.set_dict(expectedValue);
        assertEquals(0, result.free);
        assertEquals(0L, result.total);
        assertEquals(expectedValue.free, client.get_dict().free);
        assertEquals(expectedValue.total, client.get_dict().total);
    }

    @Test
    public void longArrayAttributeTest() throws IOException, TException {
        final Client client = createClient();
        final Long[] expectedValue = {3L, 90L, 50L, 7L};
        final Long[] result = ArrayUtils.toArray(client.set_longArray(ImmutableList.copyOf(expectedValue)), Long.class);
        assertArrayEquals(ArrayUtils.emptyArray(Long[].class), result);
        assertArrayEquals(expectedValue, ArrayUtils.toArray(client.get_longArray(), Long.class));
    }

    @Test
    public void notificationTest() throws IOException, TException, TimeoutException, InterruptedException {
        final NotificationSupport notifications = getManagementConnector().queryObject(NotificationSupport.class);
        final SynchronizationEvent.EventAwaitor<Notification> awaitor;
        try{
            final SynchronizationListener listener = new SynchronizationListener();
            notifications.addNotificationListener(listener, null, null);
            awaitor = listener.getAwaitor();
        }
        finally {
            releaseManagementConnector();
        }
        final Client client = createClient();
        final long timeStamp;
        client.notify_testEvent("Frank Underwood", 1L, timeStamp = System.currentTimeMillis(), 42L);
        final Notification n = awaitor.await(TimeSpan.fromSeconds(100000L));
        assertEquals("Frank Underwood", n.getMessage());
        assertEquals(1L, n.getSequenceNumber());
        assertEquals(timeStamp, n.getTimeStamp());
        assertEquals(42L, n.getUserData());
        //verify that Thrift binary stream is parsed successfully
        assertEquals(0L, client.get_long());
    }

    @Override
    protected void fillEvents(final Map<String, EventConfiguration> events, final Supplier<EventConfiguration> eventFactory) {
        EventConfiguration event = eventFactory.get();
        event.setCategory("testEvent");
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
        attr.getParameters().put("dictionaryItemTypes", "int32, int64");
        attributes.put("attr8", attr);

        attr = attributeFactory.get();
        attr.setAttributeName("longArray");
        attr.getParameters().put("expectedType", "array(int64)");
        attributes.put("attr9", attr);
    }
}
