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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
    public void setupHazelcastNodes(){
        instance1 = new GridMember();
        instance2 = new GridMember();
    }

    @After
    public void shutdownHazelcastNodes() throws InterruptedException {
        instance1.close();
        instance2.close();
        instance1 = null;
        instance2 = null;
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
        final Future<? extends Communicator.IncomingMessage> receiver2 = com2.receiveMessage(Communicator.MessageType.SIGNAL);
        com1.sendSignal("Request");
        assertEquals("Request", receiver2.get(1, TimeUnit.SECONDS).getPayload());
        instance1.releaseService("hzCommunicator", ClusterMember.COMMUNICATION_SERVICE);
    }

    @Test
    public void communicatorTest() throws InterruptedException, TimeoutException, ExecutionException {
        final Communicator com = instance1.getService("hzCommunicator", ClusterMember.COMMUNICATION_SERVICE);
        assertTrue(com instanceof HazelcastCommunicator);
        //test message box
        try(final Communicator.MessageBox box = com.createMessageBox(Communicator.ANY_MESSAGE)) {
            com.sendSignal("First");
            com.sendSignal("Second");
            Thread.sleep(300);
            assertEquals(2, box.size());
            assertEquals("First", box.poll().getPayload());
            assertEquals("Second", box.poll().getPayload());
        }
        //test future
        final Future<? extends Communicator.IncomingMessage> receiver1 = com.receiveMessage(Communicator.ANY_MESSAGE);
        final Future<? extends Communicator.IncomingMessage> receiver2 = com.receiveMessage(Communicator.ANY_MESSAGE);
        com.sendSignal("Hello");
        assertEquals("Hello", receiver1.get(1, TimeUnit.SECONDS).getPayload());
        assertEquals("Hello", receiver2.get(1, TimeUnit.SECONDS).getPayload());
        //test dialog
        final String EXPECTED_RESPONSE = "Response";
        final Consumer<Communicator.IncomingMessage> responseListener = msg -> {
            assertNotNull(msg.getSender());
            assertTrue(msg.getSender().isActive());
            assertEquals("Request", msg.getPayload());
            com.sendMessage(EXPECTED_RESPONSE, Communicator.MessageType.RESPONSE, msg.getMessageID());
        };
        try(final SafeCloseable ignored = com.addMessageListener(responseListener, Communicator.MessageType.REQUEST)){
            final Future<? extends Communicator.IncomingMessage> response = com.sendRequest("Request");
            assertEquals(EXPECTED_RESPONSE, response.get(1, TimeUnit.SECONDS).getPayload());
        }
        try(final SafeCloseable ignored = com.addMessageListener(responseListener, Communicator.MessageType.REQUEST)){
            final Communicator.IncomingMessage response = com.sendRequest("Request", Duration.ofSeconds(1L));
            assertEquals(EXPECTED_RESPONSE, response.getPayload());
        }
        instance1.releaseService("hzCommunicator", ClusterMember.COMMUNICATION_SERVICE);
    }
}
