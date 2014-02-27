package com.snamp.connectors;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.snamp.SnampClassTestSet;
import org.junit.Test;
import static com.snamp.connectors.NotificationSupport.Notification;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

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
        final Object mqmessage = "MQ message";
        final Notification notif = parser.createNotification(mqmessage);
        assertEquals(mqmessage.toString(), notif.getMessage());
        assertEquals(Notification.Severity.DEBUG, notif.getSeverity());
        assertEquals(10L, notif.getSequenceNumber());
    }
}
