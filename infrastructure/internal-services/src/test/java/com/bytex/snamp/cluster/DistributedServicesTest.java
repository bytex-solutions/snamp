package com.bytex.snamp.cluster;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.core.Communicator;
import com.bytex.snamp.core.KeyValueStorage;
import com.bytex.snamp.core.SharedCounter;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents tests for distributed services with hazelcast.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class DistributedServicesTest extends Assert {

    private GridMember instance1;
    private GridMember instance2;

    @Before
    public void setupHazelcastNodes() throws Exception {
        instance1 = GridMember.bootstrap();
        //instance1.startupFromConfiguration().activate();
        instance2 = GridMember.bootstrap();
        //instance2.startupFromConfiguration().activate();
    }

    @After
    public void shutdownHazelcastNodes() throws Exception {
        instance1.destroyLocalServices();
        instance1.close();
        instance2.destroyLocalServices();
        instance2.close();
        instance1 = null;
        instance2 = null;
    }

    @Test
    public void storageTest() throws InterruptedException {
        final KeyValueStorage storage = instance1.getKeyValueDatabases(false).getSharedObject("testStorage");
        KeyValueStorage.TextRecordView textRecord = storage.getOrCreateRecord("String Key", KeyValueStorage.TextRecordView.class, KeyValueStorage.TextRecordView.INITIALIZER);
        textRecord.setAsText("Hello, world");
        textRecord = storage.getRecord("String Key", KeyValueStorage.TextRecordView.class).get();
        assertNotNull(textRecord);
        assertEquals("Hello, world", textRecord.getAsText());
        final KeyValueStorage storage2 = instance2.getKeyValueDatabases(false).getSharedObject("testStorage");
        KeyValueStorage.MapRecordView mapRecord = storage2.getOrCreateRecord("NewMap", KeyValueStorage.MapRecordView.class, KeyValueStorage.MapRecordView.INITIALIZER);
        mapRecord.setAsMap(ImmutableMap.of("key1", "value1", "key2", "value2"));
        mapRecord = storage.getRecord("NewMap", KeyValueStorage.MapRecordView.class).get();
        assertEquals("value1", mapRecord.getAsMap().get("key1"));
        assertEquals("value2", mapRecord.getAsMap().get("key2"));
    }

    @Test
    public void counterTest() throws InterruptedException {
        final SharedCounter counter1 = instance1.getCounters().getSharedObject("testCounter");
        final SharedCounter counter2 = instance2.getCounters().getSharedObject("testCounter");
        counter1.getAsLong();
        counter1.getAsLong();
        counter2.getAsLong();
        Thread.sleep(300);
        assertEquals(3L, counter1.getAsLong());
        instance1.getCounters().releaseSharedObject("testCounter");
    }

    @Test
    public void dialogTest() throws InterruptedException, ExecutionException, TimeoutException {
        final Communicator com1 = instance1.getCommunicators().getSharedObject("hzCommunicator");
        final Communicator com2 = instance2.getCommunicators().getSharedObject("hzCommunicator");
        assertTrue(com1 instanceof HazelcastCommunicator);
        assertTrue(com2 instanceof HazelcastCommunicator);
        final Future<String> receiver2 = com2.receiveMessage(Communicator.MessageType.SIGNAL, Communicator::getPayloadAsString);
        com1.sendSignal("Request");
        assertEquals("Request", receiver2.get(1, TimeUnit.SECONDS));
        instance1.getCommunicators().releaseSharedObject("hzCommunicator");
    }

    @Test
    public void communicatorTest() throws InterruptedException, TimeoutException, ExecutionException {
        final Communicator communicator1 = instance1.getCommunicators().getSharedObject("hzCommunicator");
        final Communicator communicator2 = instance2.getCommunicators().getSharedObject("hzCommunicator");
        assertTrue(communicator1 instanceof HazelcastCommunicator);
        //test message box
        try (final Communicator.MessageBox<String> box = communicator1.createMessageBox(Communicator.ANY_MESSAGE, Communicator::getPayloadAsString)) {
            communicator1.sendSignal("First");
            communicator1.sendSignal("Second");
            Thread.sleep(300);
            assertEquals(2, box.size());
            assertEquals("First", box.poll());
            assertEquals("Second", box.poll());
        }
        //test future
        final Future<String> receiver1 = communicator1.receiveMessage(Communicator.ANY_MESSAGE, Communicator::getPayloadAsString);
        final Future<String> receiver2 = communicator1.receiveMessage(Communicator.ANY_MESSAGE, Communicator::getPayloadAsString);
        communicator1.sendSignal("Hello");
        assertEquals("Hello", receiver1.get(1, TimeUnit.SECONDS));
        assertEquals("Hello", receiver2.get(1, TimeUnit.SECONDS));
        //test dialog
        final String EXPECTED_RESPONSE = "Response";
        final Communicator.MessageListener responseListener = msg -> {
            assertNotNull(msg.getSource());
            //assertTrue(msg.getSender().isActive());
            assertEquals("Request", msg.getPayload());
            communicator1.sendMessage(EXPECTED_RESPONSE, Communicator.MessageType.RESPONSE, msg.getMessageID());
        };
        try (final SafeCloseable ignored = communicator1.addMessageListener(responseListener, Communicator.MessageType.REQUEST)) {
            final Future<String> response = communicator2.sendRequest("Request", Communicator::getPayloadAsString);
            assertEquals(EXPECTED_RESPONSE, response.get(1, TimeUnit.SECONDS));
        }
        try (final SafeCloseable ignored = communicator1.addMessageListener(responseListener, Communicator.MessageType.REQUEST)) {
            final String response = communicator2.sendRequest("Request", Communicator::getPayloadAsString, Duration.ofSeconds(1L));
            assertEquals(EXPECTED_RESPONSE, response);
        }
        instance1.getCommunicators().releaseSharedObject("hzCommunicator");
    }
}
