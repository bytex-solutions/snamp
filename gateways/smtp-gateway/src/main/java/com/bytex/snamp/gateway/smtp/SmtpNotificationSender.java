package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.modeling.NotificationRouter;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.json.JsonUtils;
import com.google.common.net.MediaType;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class SmtpNotificationSender extends NotificationRouter {
    private static final ObjectWriter FORMATTER;

    static {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JsonUtils());
        FORMATTER = mapper.writerWithDefaultPrettyPrinter();
    }

    /**
     * Initializes a new notification router.
     * <p>
     * Note that the notification router holds a weak reference
     *
     * @param resourceName Name of the managed resource bounded to this router.
     * @param metadata     The metadata of the notification. Cannot be {@literal null}.
     * @param destination  The notification acceptor.
     */
    SmtpNotificationSender(final String resourceName,
                                  final MBeanNotificationInfo metadata,
                                  final NotificationListener destination) {
        super(resourceName, metadata, destination);
    }

    static void sendMessage(final String resourceName,
                            final MailMessageFactory messageFactory,
                     final Notification notification) throws MessagingException {
        final Message message = messageFactory.call();
        message.setSubject(resourceName + ':' + notification.getType());
        final MimeMultipart attachments = new MimeMultipart();
        //set message text
        MimeBodyPart part = new MimeBodyPart();
        part.setText("Hello", IOUtils.DEFAULT_CHARSET.name());
        attachments.addBodyPart(part);
        //save notification as attachment
        part = new MimeBodyPart();
        part.setFileName("notification.json");
        part.setDescription("Raw notification in JSON format");
        try {
            final String rawNotification = FORMATTER.writeValueAsString(notification);
            part.setDataHandler(new DataHandler(rawNotification, MediaType.JSON_UTF_8.toString()));
        } catch (final IOException e) {
            throw new MessagingException("Unable to serialize notification into JSON", e);
        }
        attachments.addBodyPart(part);
        message.setContent(attachments);
        //save changes and send
        message.saveChanges();
        Transport.send(message);
    }
}
