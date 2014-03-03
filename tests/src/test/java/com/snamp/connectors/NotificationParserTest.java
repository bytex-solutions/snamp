package com.snamp.connectors;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.snamp.SnampClassTestSet;
import org.junit.Test;
import static com.snamp.connectors.NotificationSupport.Notification;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class NotificationParserTest extends SnampClassTestSet<NotificationParser> {

    @Test
    public void loadAndParseTest() throws IOException, MQException {
        final String path = this.getClass().getResource("/parser.g").getPath().replaceFirst("parser.g", "");
        final NotificationParser parser = new NotificationParser("parser.g", path);
        final MQMessage mqmessage = new MQMessage();
        mqmessage.setObjectProperty("severity", Notification.Severity.DEBUG);
        mqmessage.setInt8Property("seqnum", 10L);    //sequence number
        final Date timeStamp = new Date();
        mqmessage.setObjectProperty("timestamp", timeStamp);
        final List<Integer> attachement = Arrays.asList(2, 3, 4);
        mqmessage.setObjectProperty("attachment", attachement);
        final String message = "MQ notification";
        mqmessage.writeString(message);
        mqmessage.setDataOffset(0);
        final Notification notif = parser.createNotification(mqmessage);
        assertEquals(message, notif.getMessage());
        assertEquals(Notification.Severity.DEBUG, notif.getSeverity());
        assertEquals(10L, notif.getSequenceNumber());
        assertEquals(timeStamp, notif.getTimeStamp());
        assertTrue(notif.get("attachment") instanceof List);
        assertArrayEquals(attachement.toArray(), ((List<Integer>)notif.get("attachment")).toArray());
    }
}
