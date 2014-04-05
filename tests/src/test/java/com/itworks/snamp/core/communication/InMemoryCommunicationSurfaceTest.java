package com.itworks.snamp.core.communication;

import com.itworks.snamp.SnampClassTestSet;
import com.itworks.snamp.TimeSpan;
import org.apache.commons.collections4.Predicate;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class InMemoryCommunicationSurfaceTest extends SnampClassTestSet<InMemoryCommunicator> {
    private static final class TestMessageDescriptor extends MessageDescriptorImpl<String, Boolean> {

        public TestMessageDescriptor(){
            super(String.class, Boolean.class);
        }
    }

    private static interface  TestSender extends CommunicableObject {
        void sendTestMessage() throws Exception;
    }

    private static interface TestReceiver extends CommunicableObject{

    }

    @Test
    public final void unicastTest() throws Exception {
        final String stringMessage = "Hello, world!";
        final TestReceiver obj1 = new TestReceiver() {
            private Communicator messenger;

            @Override
            public boolean connect(final Communicator messenger) {
                this.messenger = messenger;
                return true;
            }

            /**
             * Disconnects from the communicator.
             */
            @Override
            public void disconnect() {
                messenger = null;
            }

            @Override
            public <REQ, RES> RES processMessage(final CommunicableObject sender, final InputMessage<REQ, RES> message) throws Exception {
                assertTrue(sender instanceof CommunicableObject);
                assertEquals(stringMessage, message.getPayload());
                assertTrue(message.getDescriptor() instanceof TestMessageDescriptor);
                return message.getDescriptor().getOutputMessagePayloadType().cast(true);
            }
        };
        final TestSender obj2 = new TestSender() {
            private Communicator messenger;
            @Override
            public boolean connect(final Communicator messenger) {
                this.messenger = messenger;
                return true;
            }

            /**
             * Disconnects from the communicator.
             */
            @Override
            public void disconnect() {
                messenger = null;
            }

            @Override
            public <REQ, RES> RES processMessage(final CommunicableObject sender, final InputMessage<REQ, RES> message) throws Exception {
                throw new UnsupportedOperationException();
            }

            @Override
            public void sendTestMessage() throws Exception{
                assertTrue(messenger.sendMessage(this, new TestMessageDescriptor(), stringMessage, 2, new Predicate<CommunicableObject>() {
                    @Override
                    public boolean evaluate(final CommunicableObject candidate) {
                        return candidate instanceof TestReceiver;
                    }
                }, new TimeSpan(3, TimeUnit.SECONDS)));
            }
        };
        final InMemoryCommunicator surface = new InMemoryCommunicator(2) {
            @Override
            protected Iterable<CommunicableObject> getReceivers() {
                return Arrays.asList(obj1, obj2);
            }
        };
        obj1.connect(surface);
        obj2.connect(surface);
        obj2.sendTestMessage();
        surface.close();
    }
}
