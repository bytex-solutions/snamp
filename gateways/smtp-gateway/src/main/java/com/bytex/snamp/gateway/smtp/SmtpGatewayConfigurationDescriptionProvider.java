package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.Convert;
import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.gateway.GatewayDescriptionProvider;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.management.MBeanNotificationInfo;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.getValue;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SmtpGatewayConfigurationDescriptionProvider extends ConfigurationEntityDescriptionProviderImpl implements GatewayDescriptionProvider {
    private static final Splitter MAIL_SPLITTER = Splitter.on(';').trimResults().omitEmptyStrings();
    private static final String USER_NAME_PARAM = "userName";
    private static final String PASSWORD_PARAM = "password";
    private static final String SENDER_PARAM = "from";
    private static final String RECEIVERS_PARAM = "to";
    private static final String COPY_RECEIVERS_PARAMS = "Cc";
    private static final String NOTIF_TO_EMAIL = "sendToEmail";

    private static final LazySoftReference<SmtpGatewayConfigurationDescriptionProvider> INSTANCE = new LazySoftReference<>();

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
        return DescriptorUtils.getField(metadata.getDescriptor(), NOTIF_TO_EMAIL, Convert::toBoolean)
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
                .putAll(Message.RecipientType.CC, parseAddresses(parameters.getOrDefault(COPY_RECEIVERS_PARAMS, "")))
                .build();
    }

    InternetAddress parseSender(final Map<String, String> parameters) throws SmtpGatewayAbsentConfigurationParameterException, AddressException {
        final String sender = getValue(parameters, SENDER_PARAM, Function.identity()).orElseThrow(() -> new SmtpGatewayAbsentConfigurationParameterException(SENDER_PARAM));
        return new InternetAddress(sender);
    }
}
