package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.gateway.modeling.NotificationAccessor;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.json.JsonUtils;
import com.google.common.net.MediaType;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.compiler.CompiledST;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class SmtpNotificationSender extends NotificationAccessor {
    private static final ObjectWriter FORMATTER;
    private static final STGroup TEMPLATE_GROUP;

    static {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JsonUtils());
        FORMATTER = mapper.writerWithDefaultPrettyPrinter();
        TEMPLATE_GROUP = new STGroup('{', '}');
    }

    private MailMessageFactory messageFactory;
    private final CompiledST mailTemplate;      //pre-compiled template
    private final String resourceName;
    private final Logger logger;

    SmtpNotificationSender(final String resourceName,
                           final MBeanNotificationInfo metadata) {
        super(metadata);
        mailTemplate = SmtpGatewayConfigurationDescriptionProvider.getNotificationTemplate(metadata);
        this.resourceName = resourceName;
        logger = LoggerProvider.getLoggerForObject(this);
    }

    void setMessageFactory(final MailMessageFactory value){
        messageFactory = Objects.requireNonNull(value);
    }

    private ST prepareRenderer(final Notification notification) throws CloneNotSupportedException {
        final ST template = TEMPLATE_GROUP.createStringTemplate(mailTemplate.clone());
        template.add("timeStamp", Instant.ofEpochMilli(notification.getTimeStamp()));
        template.add("notificationType", notification.getType());
        template.add("message", notification.getMessage());
        template.add("sequenceNumber", notification.getSequenceNumber());
        template.add("source", notification.getSource());
        template.add("resourceName", resourceName);
        template.add("description", getMetadata().getDescription());
        template.add("severity", NotificationDescriptor.getSeverity(getMetadata()));
        return template;
    }

    private void sendNotificationToMail(final Notification notification) throws MessagingException, IOException, CloneNotSupportedException {
        final MailMessageFactory factory = messageFactory;
        if (factory == null)
            return;
        final Message message = factory.call();
        message.setSubject(resourceName + ':' + notification.getType());
        final MimeMultipart attachments = new MimeMultipart();
        //set message text
        MimeBodyPart part = new MimeBodyPart();
        part.setText(prepareRenderer(notification).render(), IOUtils.DEFAULT_CHARSET.name());
        attachments.addBodyPart(part);
        //save notification as attachment
        part = new MimeBodyPart();
        part.setFileName("notification.json");
        part.setDescription("Raw notification in JSON format");
        final String rawNotification = FORMATTER.writeValueAsString(notification);
        part.setDataHandler(new DataHandler(rawNotification, MediaType.JSON_UTF_8.toString()));
        attachments.addBodyPart(part);
        message.setContent(attachments);
        Transport.send(message);
    }

    /**
     * Invoked when a JMX notification occurs.
     * The implementation of this method should return as soon as possible, to avoid
     * blocking its notification broadcaster.
     *
     * @param notification The notification.
     * @param handback     An opaque object which helps the listener to associate
     *                     information regarding the MBean emitter. This object is passed to the
     *                     addNotificationListener call and resent, without modification, to the
     */
    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        try {
            sendNotificationToMail(notification);
        } catch (final MessagingException | IOException | CloneNotSupportedException e) {
            logger.log(Level.SEVERE, "Unable to send e-mail", e);
        }
    }

    /**
     * Disconnects notification accessor from the managed resource.
     */
    @Override
    public void close() {
        messageFactory = null;
        super.close();
    }
}
