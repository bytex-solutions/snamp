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
    private final ObjectWriter jsonSerializer;
    private MailMessageFactory messageFactory;
    private CompiledST mailTemplate;      //pre-compiled template
    private final String resourceName;
    private final Logger logger;

    SmtpNotificationSender(final String resourceName,
                           final MBeanNotificationInfo metadata) {
        super(metadata);
        mailTemplate = SmtpGatewayConfigurationDescriptionProvider.getNotificationTemplate(metadata);
        this.resourceName = resourceName;
        logger = LoggerProvider.getLoggerForObject(this);
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JsonUtils());
        jsonSerializer = mapper.writerWithDefaultPrettyPrinter();
    }

    void setMessageFactory(final MailMessageFactory value){
        messageFactory = Objects.requireNonNull(value);
        mailTemplate = value.compileNotificationTemplate(getMetadata());
    }

    private static ST prepareRenderer(final CompiledST mailTemplate,
                                      final Notification notification,
                                      final MBeanNotificationInfo metadata,
                                      final String resourceName) {
        return DefaultMailTemplate.createTemplateRenderer(mailTemplate)
                .add("timeStamp", Instant.ofEpochMilli(notification.getTimeStamp()))
                .add("notificationType", notification.getType())
                .add("message", notification.getMessage())
                .add("sequenceNumber", notification.getSequenceNumber())
                .add("source", notification.getSource())
                .add("resourceName", resourceName)
                .add("description", metadata.getDescription())
                .add("severity", NotificationDescriptor.getSeverity(metadata));
    }

    private void sendNotificationToMail(final Notification notification) throws MessagingException, IOException {
        final MailMessageFactory messageFactory = this.messageFactory;
        final CompiledST mailTemplate = this.mailTemplate;
        if (messageFactory == null || mailTemplate == null)
            return;
        final Message message = messageFactory.createMessage();
        message.setSubject("Notification from " + resourceName + ':' + notification.getType());
        final MimeMultipart attachments = new MimeMultipart();
        //set message text
        MimeBodyPart part = new MimeBodyPart();
        part.setText(prepareRenderer(mailTemplate, notification, getMetadata(), resourceName).render(), IOUtils.DEFAULT_CHARSET.name());
        attachments.addBodyPart(part);
        //save notification as attachment
        part = new MimeBodyPart();
        part.setFileName("notification.json");
        part.setDescription("Raw notification in JSON format");
        final String rawNotification = jsonSerializer.writeValueAsString(notification);
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
        } catch (final MessagingException | IOException e) {
            logger.log(Level.SEVERE, "Unable to send e-mail", e);
        }
    }

    /**
     * Disconnects notification accessor from the managed resource.
     */
    @Override
    public void close() {
        messageFactory = null;
        mailTemplate = null;
        super.close();
    }
}
