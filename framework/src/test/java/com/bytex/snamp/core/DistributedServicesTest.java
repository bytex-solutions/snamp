package com.bytex.snamp.core;

import com.bytex.snamp.SafeCloseable;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class DistributedServicesTest extends Assert {
    @Test
    public void idGeneratorTest(){
        assertEquals(0L, DistributedServices.getProcessLocalCounterGenerator("gen1").getAsLong());
        assertEquals(1L, DistributedServices.getProcessLocalCounterGenerator("gen1").getAsLong());
        assertEquals(0L, DistributedServices.getProcessLocalCounterGenerator("gen2").getAsLong());
    }

    @Test
    public void storageTest(){
        DistributedServices.getProcessLocalStorage("collection1").put("k1", 42L);
        DistributedServices.getProcessLocalStorage("collection2").put("k1", 43L);
        assertEquals(42L, DistributedServices.getProcessLocalStorage("collection1").get("k1"));
        assertEquals(43L, DistributedServices.getProcessLocalStorage("collection2").get("k1"));
    }

    @Test
    public void communicatorTest() throws InterruptedException, ExecutionException, TimeoutException {
        final Communicator com = DistributedServices.getProcessLocalCommunicator("localCommunicator");
        assertTrue(com instanceof LocalCommunicator);
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
        assertFalse(((LocalCommunicator)com).hasNoSubscribers());
        com.sendSignal("Hello");
        assertEquals("Hello", receiver1.get(1, TimeUnit.SECONDS).getPayload());
        assertEquals("Hello", receiver2.get(1, TimeUnit.SECONDS).getPayload());
        //test dialog
        final String EXPECTED_RESPONSE = "Response";
        final Consumer<Communicator.IncomingMessage> responseListener = msg -> {
            //avoid cyclic message
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
        assertTrue(((LocalCommunicator)com).hasNoSubscribers());
    }
}
