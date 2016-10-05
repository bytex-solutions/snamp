package com.bytex.snamp.core;

import com.bytex.snamp.SafeCloseable;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class LocalServicesTest extends Assert {
    private static final Function<Communicator.IncomingMessage, String> PAYLOAD_TO_STRING = msg -> msg.getPayload().toString();

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
        assertTrue(com instanceof LocalCommunicator);//test message box
        try (final Communicator.MessageBox<String> box = com.createMessageBox(Communicator.ANY_MESSAGE, PAYLOAD_TO_STRING)) {
            com.sendSignal("First");
            com.sendSignal("Second");
            Thread.sleep(300);
            assertEquals(2, box.size());
            assertEquals("First", box.poll());
            assertEquals("Second", box.poll());
        }
        //test future
        final Future<String> receiver1 = com.receiveMessage(Communicator.ANY_MESSAGE, PAYLOAD_TO_STRING);
        final Future<String> receiver2 = com.receiveMessage(Communicator.ANY_MESSAGE, PAYLOAD_TO_STRING);
        com.sendSignal("Hello");
        assertEquals("Hello", receiver1.get(1, TimeUnit.SECONDS));
        assertEquals("Hello", receiver2.get(1, TimeUnit.SECONDS));
        //test dialog
        final String EXPECTED_RESPONSE = "Response";
        final Consumer<Communicator.IncomingMessage> responseListener = msg -> {
            //avoid cyclic message
            assertEquals("Request", msg.getPayload());
            com.sendMessage(EXPECTED_RESPONSE, Communicator.MessageType.RESPONSE, msg.getMessageID());
        };
        try (final SafeCloseable ignored = com.addMessageListener(responseListener, Communicator.MessageType.REQUEST)) {
            final Future<String> response = com.sendRequest("Request", PAYLOAD_TO_STRING);
            assertEquals(EXPECTED_RESPONSE, response.get(1, TimeUnit.SECONDS));
        }
        try (final SafeCloseable ignored = com.addMessageListener(responseListener, Communicator.MessageType.REQUEST)) {
            final String response = com.sendRequest("Request", PAYLOAD_TO_STRING, Duration.ofSeconds(1L));
            assertEquals(EXPECTED_RESPONSE, response);
        }
        assertTrue(((LocalCommunicator) com).hasNoSubscribers());
    }
}
