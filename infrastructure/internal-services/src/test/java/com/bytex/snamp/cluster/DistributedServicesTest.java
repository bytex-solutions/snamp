package com.bytex.snamp.cluster;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.Communicator;
import com.bytex.snamp.core.LongCounter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Represents tests for distributed services with hazelcast.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class DistributedServicesTest extends Assert {

    private GridMember instance1;
    private GridMember instance2;

    @Before
    public void setupHazelcastNodes() throws Exception {
        instance1 = new GridMember();
        instance1.start();
        instance2 = new GridMember();
        instance2.start();
    }

    @After
    public void shutdownHazelcastNodes() throws InterruptedException {
        instance1.shutdownAndClose();
        instance2.shutdownAndClose();
        instance1 = null;
        instance2 = null;
    }

    @Test
    public void storageTest() throws InterruptedException {
        final ConcurrentMap<String, Object> storage1 = instance1.getService("storage", ClusterMember.MAP_SERVICE);
        final ConcurrentMap<String, Object> storage2 = instance2.getService("storage", ClusterMember.MAP_SERVICE);
        assertTrue(storage1 instanceof HazelcastStorage);
        assertTrue(storage2 instanceof HazelcastStorage);
        storage1.put("key", Duration.ofSeconds(10));
        Thread.sleep(400);
        final Object value = storage2.get("key");
        assertTrue(value instanceof Duration);
        assertEquals(Duration.ofSeconds(10), value);
    }

    @Test
    public void counterTest() throws InterruptedException {
        final LongCounter counter1 = instance1.getService("testCounter", ClusterMember.IDGEN_SERVICE);
        final LongCounter counter2 = instance2.getService("testCounter", ClusterMember.IDGEN_SERVICE);
        assertNotNull(counter1);
        assertNotNull(counter2);
        counter1.getAsLong();
        counter1.getAsLong();
        counter2.getAsLong();
        Thread.sleep(300);
        assertEquals(3L, counter1.getAsLong());
        instance1.releaseService("testCounter", ClusterMember.IDGEN_SERVICE);
    }

    @Test
    public void dialogTest() throws InterruptedException, ExecutionException, TimeoutException {
        final Communicator com1 = instance1.getService("hzCommunicator", ClusterMember.COMMUNICATION_SERVICE);
        final Communicator com2 = instance2.getService("hzCommunicator", ClusterMember.COMMUNICATION_SERVICE);
        assertTrue(com1 instanceof HazelcastCommunicator);
        assertTrue(com2 instanceof HazelcastCommunicator);
        final Future<String> receiver2 = com2.receiveMessage(Communicator.MessageType.SIGNAL, Communicator::getPayloadAsString);
        com1.sendSignal("Request");
        assertEquals("Request", receiver2.get(1, TimeUnit.SECONDS));
        instance1.releaseService("hzCommunicator", ClusterMember.COMMUNICATION_SERVICE);
    }

    @Test
    public void communicatorTest() throws InterruptedException, TimeoutException, ExecutionException {
        final Communicator com = instance1.getService("hzCommunicator", ClusterMember.COMMUNICATION_SERVICE);
        assertTrue(com instanceof HazelcastCommunicator);
        //test message box
        try (final Communicator.MessageBox<String> box = com.createMessageBox(Communicator.ANY_MESSAGE, Communicator::getPayloadAsString)) {
            com.sendSignal("First");
            com.sendSignal("Second");
            Thread.sleep(300);
            assertEquals(2, box.size());
            assertEquals("First", box.poll());
            assertEquals("Second", box.poll());
        }
        //test future
        final Future<String> receiver1 = com.receiveMessage(Communicator.ANY_MESSAGE, Communicator::getPayloadAsString);
        final Future<String> receiver2 = com.receiveMessage(Communicator.ANY_MESSAGE, Communicator::getPayloadAsString);
        com.sendSignal("Hello");
        assertEquals("Hello", receiver1.get(1, TimeUnit.SECONDS));
        assertEquals("Hello", receiver2.get(1, TimeUnit.SECONDS));
        //test dialog
        final String EXPECTED_RESPONSE = "Response";
        final Consumer<Communicator.IncomingMessage> responseListener = msg -> {
            assertNotNull(msg.getSender());
            assertTrue(msg.getSender().isActive());
            assertEquals("Request", msg.getPayload());
            com.sendMessage(EXPECTED_RESPONSE, Communicator.MessageType.RESPONSE, msg.getMessageID());
        };
        try (final SafeCloseable ignored = com.addMessageListener(responseListener, Communicator.MessageType.REQUEST)) {
            final Future<String> response = com.sendRequest("Request", Communicator::getPayloadAsString);
            assertEquals(EXPECTED_RESPONSE, response.get(1, TimeUnit.SECONDS));
        }
        try (final SafeCloseable ignored = com.addMessageListener(responseListener, Communicator.MessageType.REQUEST)) {
            final String response = com.sendRequest("Request", Communicator::getPayloadAsString, Duration.ofSeconds(1L));
            assertEquals(EXPECTED_RESPONSE, response);
        }
        instance1.releaseService("hzCommunicator", ClusterMember.COMMUNICATION_SERVICE);
    }
}
