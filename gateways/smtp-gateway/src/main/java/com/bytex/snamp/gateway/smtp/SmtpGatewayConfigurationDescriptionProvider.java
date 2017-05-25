package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.Convert;
import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.gateway.GatewayDescriptionProvider;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.compiler.CompiledST;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.management.MBeanNotificationInfo;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.getValue;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SmtpGatewayConfigurationDescriptionProvider extends ConfigurationEntityDescriptionProviderImpl implements GatewayDescriptionProvider {
    private static final class DefaultNotificationTemplateLoader{
        String getTemplate() throws IOException {
            try(final InputStream stream = getClass().getResourceAsStream("NotificationMessageTemplate.txt")){
                return IOUtils.toString(stream);
            }
        }
    }

    private static final Splitter MAIL_SPLITTER = Splitter.on(';').trimResults().omitEmptyStrings();
    private static final String USER_NAME_PARAM = "userName";
    private static final String PASSWORD_PARAM = "password";
    private static final String SENDER_PARAM = "from";
    private static final String RECEIVERS_PARAM = "to";
    private static final String COPY_RECEIVERS_PARAM = "Cc";
    //notifications
    private static final String NOTIF_TO_EMAIL_PARAM = "sendToEmail";
    private static final String EMAIL_TEMPLATE_PARAM = "mailTemplate";

    private static final STGroup TEMPLATE_GROUP;
    private static final LazySoftReference<SmtpGatewayConfigurationDescriptionProvider> INSTANCE;
    private static final CompiledST DEFAULT_NOTIFICATION_TEMPLATE;

    static {
        TEMPLATE_GROUP = new STGroup('{', '}');
        INSTANCE = new LazySoftReference<>();
        String template = Utils.callAndWrapException(new DefaultNotificationTemplateLoader()::getTemplate, ExceptionInInitializerError::new);
        DEFAULT_NOTIFICATION_TEMPLATE = TEMPLATE_GROUP.compile(TEMPLATE_GROUP.getFileName(), "DefaultNotificationMessageTemplate", null, template, null);
    }
    
    private SmtpGatewayConfigurationDescriptionProvider(){

    }

    static SmtpGatewayConfigurationDescriptionProvider getInstance(){
        return INSTANCE.lazyGet(SmtpGatewayConfigurationDescriptionProvider::new);
    }

    PasswordAuthentication parseCredentials(final Map<String, String> parameters) throws SmtpGatewayAbsentConfigurationParameterException{
        final String userName = getValue(parameters, USER_NAME_PARAM, Function.identity())
                .orElseThrow(() -> new SmtpGatewayAbsentConfigurationParameterException(USER_NAME_PARAM));
        final String password = getValue(parameters, PASSWORD_PARAM, Function.identity())
                .orElseThrow(() -> new SmtpGatewayAbsentConfigurationParameterException(PASSWORD_PARAM));
        return new PasswordAuthentication(userName, password);
    }

    static boolean sendViaEMail(final MBeanNotificationInfo metadata) {
        return DescriptorUtils.getField(metadata.getDescriptor(), NOTIF_TO_EMAIL_PARAM, Convert::toBoolean)
                .orElseGet(Optional::empty)
                .orElse(false);
    }

    private static Iterable<InternetAddress> parseAddresses(final String addresses) throws AddressException{
        final Collection<InternetAddress> result = new LinkedList<>();
        for(final String mailAddress: MAIL_SPLITTER.split(addresses))
            result.add(new InternetAddress(mailAddress));
        return result;
    }

    Multimap<Message.RecipientType, InternetAddress> parseRecipients(final Map<String, String> parameters) throws AddressException {
        return ImmutableMultimap.<Message.RecipientType, InternetAddress>builder()
                .putAll(Message.RecipientType.TO, parseAddresses(parameters.getOrDefault(RECEIVERS_PARAM, "")))
                .putAll(Message.RecipientType.CC, parseAddresses(parameters.getOrDefault(COPY_RECEIVERS_PARAM, "")))
                .build();
    }

    InternetAddress parseSender(final Map<String, String> parameters) throws SmtpGatewayAbsentConfigurationParameterException, AddressException {
        final String sender = getValue(parameters, SENDER_PARAM, Function.identity()).orElseThrow(() -> new SmtpGatewayAbsentConfigurationParameterException(SENDER_PARAM));
        return new InternetAddress(sender);
    }

    static CompiledST getNotificationTemplate(final MBeanNotificationInfo metadata) {
        return DescriptorUtils.getField(metadata.getDescriptor(), EMAIL_TEMPLATE_PARAM, Objects::toString)
                .map(templateLocation -> {
                    String template;
                    try {
                        template = IOUtils.contentAsString(new URL(templateLocation));
                    } catch (final IOException e) {
                        template = null;
                    }
                    return template;
                }).map(template -> TEMPLATE_GROUP.compile(TEMPLATE_GROUP.getFileName(), metadata.getName(), null, template, null))
                .orElse(DEFAULT_NOTIFICATION_TEMPLATE);

    }
}
