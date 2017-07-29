package com.bytex.snamp.io;

import com.bytex.snamp.connector.notifications.NotificationBuilder;
import org.junit.Assert;
import org.junit.Test;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class IOUtilsTest extends Assert {
    @Test
    public void directBufferStreamTest() throws IOException {
        try(final ByteBufferOutputStream os = new ByteBufferOutputStream(5)){
            assertEquals(0, os.getSize());
            os.write(10);
            os.write(20);
            assertEquals(2, os.getSize());
            os.write(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
            assertEquals(11, os.getSize());
            try(final InputStream is = os.getInputStream()){
                final byte[] arrays = new byte[14];
                assertEquals(11, is.available());
                assertEquals(11, is.read(arrays));
                assertEquals(0, is.available());
                assertArrayEquals(new byte[]{10, 20, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 0}, arrays);
            }
        }
    }

    @Test
    public void cloneTest() throws CloneNotSupportedException {
        final Notification origin = new NotificationBuilder()
                .setType(AttributeChangeNotification.ATTRIBUTE_CHANGE)
                .setMessage("Frank Underwood")
                .setSequenceNumber(10L)
                .setTimeStamp()
                .setUserData(new StringBuffer(1024))
                .setSource("Source")
                .get();
        final Notification cloned = IOUtils.clone(origin);
        assertEquals(origin.getType(), cloned.getType());
        assertTrue(cloned.getUserData() instanceof StringBuffer);
        assertEquals(origin.getTimeStamp(), cloned.getTimeStamp());
        assertEquals(origin.getSource(), cloned.getSource());
    }

    @Test
    public void serializationDeserializationTest() throws IOException {
        final Notification origin = new NotificationBuilder()
                .setType(AttributeChangeNotification.ATTRIBUTE_CHANGE)
                .setMessage("Frank Underwood")
                .setSequenceNumber(10L)
                .setTimeStamp()
                .setUserData(new StringBuffer(1024))
                .setSource("Source")
                .get();
        final byte[] serializedForm = IOUtils.serialize(origin);
        final Notification cloned = IOUtils.deserialize(serializedForm, Notification.class);
        assertEquals(origin.getType(), cloned.getType());
        assertTrue(cloned.getUserData() instanceof StringBuffer);
        assertEquals(origin.getTimeStamp(), cloned.getTimeStamp());
        assertEquals(origin.getSource(), cloned.getSource());
    }

    @Test
    public void serializationDeserializationTest2() throws IOException {
        final Notification origin = new NotificationBuilder()
                .setType(AttributeChangeNotification.ATTRIBUTE_CHANGE)
                .setMessage("Frank Underwood")
                .setSequenceNumber(10L)
                .setTimeStamp()
                .setUserData(new StringBuffer(1024))
                .setSource(this)    //non-serializable
                .get();
        final byte[] serializedForm = IOUtils.serialize(origin, SerializationMode.objectReplacement(IOUtilsTest.class, value -> "Barry Burton"));
        final Notification cloned = IOUtils.deserialize(serializedForm, Notification.class);
        assertEquals(origin.getType(), cloned.getType());
        assertTrue(cloned.getUserData() instanceof StringBuffer);
        assertEquals(origin.getTimeStamp(), cloned.getTimeStamp());
        assertEquals("Barry Burton", cloned.getSource());
    }
}
