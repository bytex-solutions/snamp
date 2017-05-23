package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.MapUtils;
import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.modeling.FeatureAccessor;
import com.google.common.collect.Multimap;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents SMTP gateway.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class SmtpGateway extends AbstractGateway {
    private final SmtpModelOfNotifications notifications;

    SmtpGateway(final String instanceName) {
        super(instanceName);
        notifications = new SmtpModelOfNotifications();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>) notifications.addNotification(resourceName, (MBeanNotificationInfo) feature);
        else
            return null;
    }

    @Override
    protected Stream<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) {
        return notifications.clear(resourceName).stream();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>) notifications.removeNotification(resourceName, (MBeanNotificationInfo) feature);
        else
            return null;
    }

    private static Authenticator createAuthenticator(final PasswordAuthentication credentials){
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return credentials;
            }
        };
    }

    private static MailMessageFactory createMessageFactory(final InternetAddress sender,
                                                           final Session mailSession,
                                                           final Multimap<Message.RecipientType, InternetAddress> recipients) {
        return () -> {
            final Message message = new MimeMessage(mailSession);
            message.setFlag(Flags.Flag.FLAGGED, true);
            message.setSentDate(new Date());
            message.setFrom(sender);
            for (final Map.Entry<Message.RecipientType, InternetAddress> recipient : recipients.entries())
                message.addRecipient(recipient.getKey(), recipient.getValue());
            return message;
        };
    }

    @Override
    protected void start(final Map<String, String> parameters) throws SmtpGatewayAbsentConfigurationParameterException, AddressException {
        final SmtpGatewayConfigurationDescriptionProvider parser = SmtpGatewayConfigurationDescriptionProvider.getInstance();
        final MailMessageFactory messageFactory = createMessageFactory(parser.parseSender(parameters),
                Session.getInstance(MapUtils.toProperties(parameters), createAuthenticator(parser.parseCredentials(parameters))),
                parser.parseRecipients(parameters));
        notifications.setMessageFactory(messageFactory);
    }

    @Override
    protected void stop() {
        notifications.clear();
    }
}
