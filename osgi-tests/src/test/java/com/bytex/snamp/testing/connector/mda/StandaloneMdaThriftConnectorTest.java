package com.bytex.snamp.testing.connector.mda;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.connector.notifications.Mailbox;
import com.bytex.snamp.connector.notifications.MailboxFactory;
import com.bytex.snamp.connector.notifications.NotificationSupport;
import com.bytex.snamp.io.Buffers;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.bytex.snamp.configuration.EntityMap;

import static com.bytex.snamp.testing.connector.mda.MonitoringDataAcceptor.Client;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class StandaloneMdaThriftConnectorTest extends AbstractMdaConnectorTest {
    public StandaloneMdaThriftConnectorTest(){
        super("thrift://localhost:9540", ImmutableMap.of("socketTimeout", "10000"));
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
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
    public void resetTest() throws IOException, TException{
        final Client client = createClient();
        final String expectedValue = "Frank Underwood";
        assertEquals("", client.set_str(expectedValue));
        assertEquals(expectedValue, client.get_str());
        client.reset();
        assertEquals("", client.get_str());
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
        final Long[] result = client.set_longArray(ImmutableList.copyOf(expectedValue)).stream().toArray(Long[]::new);
        assertArrayEquals(ArrayUtils.emptyArray(Long[].class), result);
        assertArrayEquals(expectedValue, client.get_longArray().stream().toArray(Long[]::new));
    }

    @Test
    public void notificationTest() throws IOException, TException, TimeoutException, InterruptedException {
        final NotificationSupport notifications = getManagementConnector().queryObject(NotificationSupport.class);
        final Mailbox mailbox = MailboxFactory.newFixedSizeMailbox(2);
        try {
            notifications.addNotificationListener(mailbox, null, null);
        } finally {
            releaseManagementConnector();
        }
        final Client client = createClient();
        final long timeStamp;
        client.notify_testEvent("Frank Underwood", 1L, timeStamp = System.currentTimeMillis(), 42L);
        //verify that Thrift binary stream is parsed successfully
        assertEquals(0L, client.get_long());
        Notification n;
        do {
            n = mailbox.poll(1, TimeUnit.MILLISECONDS);
        } while (n == null);
        assertEquals("Frank Underwood", n.getMessage());
        assertEquals(1L, n.getSequenceNumber());
        assertEquals(timeStamp, n.getTimeStamp());
        assertEquals(42L, n.getUserData());
    }

    @Override
    protected void fillEvents(final EntityMap<? extends EventConfiguration> events) {
        EventConfiguration event = events.getOrAdd("e1");
        setFeatureName(event, "testEvent");
        event.getParameters().put("expectedType", "int64");
    }

    @Override
    protected void fillAttributes(final EntityMap<? extends AttributeConfiguration> attributes) {
        AttributeConfiguration attr = attributes.getOrAdd("attr1");
        setFeatureName(attr, "short");
        attr.getParameters().put("expectedType", "int16");

        attr = attributes.getOrAdd("attr2");
        setFeatureName(attr, "date");
        attr.getParameters().put("expectedType", "datetime");

        attr = attributes.getOrAdd("attr3");
        setFeatureName(attr, "biginteger");
        attr.getParameters().put("expectedType", "bigint");

        attr = attributes.getOrAdd("attr4");
        setFeatureName(attr, "str");
        attr.getParameters().put("expectedType", "string");

        attr = attributes.getOrAdd("attr5");
        setFeatureName(attr, "array");
        attr.getParameters().put("expectedType", "array(int8)");

        attr = attributes.getOrAdd("attr6");
        setFeatureName(attr, "boolean");
        attr.getParameters().put("expectedType", "bool");

        attr = attributes.getOrAdd("attr7");
        setFeatureName(attr, "long");
        attr.getParameters().put("expectedType", "int64");

        attr = attributes.getOrAdd("attr8");
        setFeatureName(attr, "dict");
        attr.getParameters().put("expectedType", "dictionary");
        attr.getParameters().put("dictionaryName", "MemoryStatus");
        attr.getParameters().put("dictionaryItemNames", "free, total");
        attr.getParameters().put("dictionaryItemTypes", "int32, int64");

        attr = attributes.getOrAdd("attr9");
        setFeatureName(attr, "longArray");
        attr.getParameters().put("expectedType", "array(int64)");
    }
}
