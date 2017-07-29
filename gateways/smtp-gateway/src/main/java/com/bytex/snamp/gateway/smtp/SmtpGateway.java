package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.MapUtils;
import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.modeling.FeatureAccessor;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.compiler.CompiledST;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import javax.net.ssl.SSLSocketFactory;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Represents SMTP gateway.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class SmtpGateway extends AbstractGateway {
    private static final class DefaultMailMessageFactory implements MailMessageFactory {
        private final InternetAddress sender;
        private final Session mailSession;
        private final Multimap<Message.RecipientType, InternetAddress> recipients;
        private final CompiledST healthStatusTemplate;
        private final CompiledST newResourceTemplate;
        private final CompiledST removedResourceTemplate;
        private final CompiledST scaleOutTemplate;
        private final CompiledST scaleInTemplate;
        private final CompiledST maxClusterSizeReached;

        DefaultMailMessageFactory(final Map<String, String> parameters,
                                        final SmtpGatewayConfigurationDescriptionProvider provider) throws SmtpGatewayAbsentConfigurationParameterException, AddressException {
            sender = provider.parseSender(parameters);
            final Properties mailProperties = MapUtils.toProperties(parameters);
            mailProperties.setProperty("mail.smtp.host", provider.getSmtpHost(parameters));
            mailProperties.setProperty("mail.smtp.port", provider.getSmtpPort(parameters));
            mailProperties.setProperty("mail.smtp.auth", "true");
            mailProperties.setProperty("mail.smtp.connectiontimeout", provider.getSocketTimeout(parameters));
            mailProperties.setProperty("mail.smtp.timeout", provider.getSocketTimeout(parameters));
            if(provider.isTlsEnabled(parameters)){
                mailProperties.setProperty("mail.smtp.socketFactory.port", provider.getSmtpPort(parameters));
                mailProperties.setProperty("mail.smtp.socketFactory.class", SSLSocketFactory.class.getName());
            }
            mailSession = Session.getInstance(new Properties(mailProperties), createAuthenticator(provider.parseCredentials(parameters)));
            recipients = provider.parseRecipients(parameters);
            healthStatusTemplate = provider.parseHealthStatusTemplate(parameters);
            newResourceTemplate = provider.parseNewResourceTemplate(parameters);
            removedResourceTemplate = provider.parseRemovedResourceTemplate(parameters);
            scaleOutTemplate = provider.parseScaleOutTemplate(parameters);
            scaleInTemplate = provider.parseScaleInTemplate(parameters);
            maxClusterSizeReached = provider.parseMaxClusterSizeReachedTemplate(parameters);
        }

        private static Authenticator createAuthenticator(final PasswordAuthentication credentials){
            return new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return credentials;
                }
            };
        }

        @Override
        public Message createMessage() throws MessagingException {
            final Message message = new MimeMessage(mailSession);
            message.setFlag(Flags.Flag.FLAGGED, true);
            message.setSentDate(new Date());
            message.setFrom(sender);
            for (final Map.Entry<Message.RecipientType, InternetAddress> recipient : recipients.entries())
                message.addRecipient(recipient.getKey(), recipient.getValue());
            return message;
        }

        @Override
        public CompiledST compileNotificationTemplate(final MBeanNotificationInfo metadata) {
            return SmtpGatewayConfigurationDescriptionProvider.getNotificationTemplate(metadata);
        }

        @Override
        public ST prepareMaxClusterSizeReachedTemplate() {
            return DefaultMailTemplate.createTemplateRenderer(maxClusterSizeReached);
        }

        @Override
        public ST prepareHealthStatusTemplate(){
            return DefaultMailTemplate.createTemplateRenderer(healthStatusTemplate);
        }

        @Override
        public ST prepareNewResourceTemplate(){
            return DefaultMailTemplate.createTemplateRenderer(newResourceTemplate);
        }

        @Override
        public ST prepareRemovedResourceTemplate(){
            return DefaultMailTemplate.createTemplateRenderer(removedResourceTemplate);
        }

        @Override
        public ST prepareScaleOutTemplate(){
            return DefaultMailTemplate.createTemplateRenderer(scaleOutTemplate);
        }

        @Override
        public ST prepareScaleInTemplate(){
            return DefaultMailTemplate.createTemplateRenderer(scaleInTemplate);
        }
    }

    private final SmtpModelOfNotifications notifications;
    private SmtpModelOfSupervisors supervisors;

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

    @Override
    protected void start(final Map<String, String> parameters) throws Exception {
        final SmtpGatewayConfigurationDescriptionProvider parser = SmtpGatewayConfigurationDescriptionProvider.getInstance();
        final MailMessageFactory messageFactory = new DefaultMailMessageFactory(parameters, parser);
        notifications.setMessageFactory(messageFactory);
        supervisors = new SmtpModelOfSupervisors();
        supervisors.update(ImmutableMap.of("default", messageFactory));
    }

    @Override
    protected void stop() throws Exception {
        Utils.closeAll(notifications::clear, supervisors);
    }
}
