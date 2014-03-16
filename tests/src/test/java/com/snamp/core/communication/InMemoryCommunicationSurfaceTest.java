package com.snamp.core.communication;

import com.snamp.SnampClassTestSet;
import com.snamp.SynchronizationEvent;
import com.snamp.TimeSpan;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class InMemoryCommunicationSurfaceTest extends SnampClassTestSet<InMemoryCommunicationSurface> {
    private static final class TestMessageDescriptor extends MessageDescriptorImpl<String, Boolean>{

        public TestMessageDescriptor(){
            super(String.class, Boolean.class);
        }
    }

    private static interface  TestSender extends CommunicableObject{
        void sendTestMessage() throws Exception;
    }

    private static interface TestReceiver extends CommunicableObject{

    }

    @Test
    public final void unicastTest() throws Exception {
        final String stringMessage = "Hello, world!";
        final CommunicationSurface surface = new InMemoryCommunicationSurface(2);
        final TestReceiver obj1 = new TestReceiver() {
            private Messenger messenger;

            @Override
            public boolean connect(final Messenger messenger) {
                this.messenger = messenger;
                return true;
            }

            @Override
            public void disconnect() {
                this.messenger = null;
            }

            @Override
            public <REQ, RES> RES processMessage(final Object sender, final InputMessage<REQ, RES> message) throws Exception {
                assertTrue(sender instanceof CommunicableObject);
                assertEquals(stringMessage, message.getPayload());
                assertTrue(message.getDescriptor() instanceof TestMessageDescriptor);
                return message.getDescriptor().getOutputMessagePayloadType().cast(true);
            }
        };
        final TestSender obj2 = new TestSender() {
            private Messenger messenger;
            @Override
            public boolean connect(final Messenger messenger) {
                this.messenger = messenger;
                return true;
            }

            @Override
            public void disconnect() {
                this.messenger = null;
            }

            @Override
            public <REQ, RES> RES processMessage(final Object sender, final InputMessage<REQ, RES> message) throws Exception {
                throw new UnsupportedOperationException();
            }

            @Override
            public void sendTestMessage() throws Exception{
                assertTrue(messenger.sendMessage(new TestMessageDescriptor(), stringMessage, 2, new ReceiverSelector() {
                    @Override
                    public boolean match(final Object candidate) {
                        return candidate instanceof TestReceiver;
                    }
                }, new TimeSpan(3, TimeUnit.SECONDS)));
            }
        };
        assertTrue(surface.registerObject(obj1));
        assertTrue(surface.registerObject(obj2));
        obj2.sendTestMessage();
        assertTrue(surface.removeObject(obj1));
        assertTrue(surface.removeObject(obj2));
        surface.close();
    }
}
