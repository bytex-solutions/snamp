package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.Convert;
import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.gateway.GatewayDescriptionProvider;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.stringtemplate.v4.compiler.CompiledST;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.management.MBeanNotificationInfo;
import java.util.*;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SmtpGatewayConfigurationDescriptionProvider extends ConfigurationEntityDescriptionProviderImpl implements GatewayDescriptionProvider {
    private static final Splitter MAIL_SPLITTER = Splitter.on(';').trimResults().omitEmptyStrings();
    //gateway
    private static final String USER_NAME_PARAM = "userName";
    private static final String PASSWORD_PARAM = "password";
    private static final String SENDER_PARAM = "from";
    private static final String RECEIVERS_PARAM = "to";
    private static final String COPY_RECEIVERS_PARAM = "Cc";
    private static final String HEALTH_STATUS_TEMPLATE_PARAM = "healthStatusTemplate";
    private static final String NEW_RESOURCE_TEMPLATE_PARAM = "newResourceTemplate";
    private static final String REMOVED_RESOURCE_TEMPLATE_PARAM = "removedResourceTemplate";
    private static final String SCALE_OUT_TEMPLATE_PARAM = "scaleOutTemplate";
    private static final String SCALE_IN_TEMPLATE_PARAM = "scaleInTemplate";
    private static final String MAX_CLUSTER_SIZE_TEMPLATE_PARAM = "maxClusterSizeReachedTemplate";
    //notifications
    private static final String NOTIF_TO_EMAIL_PARAM = "sendToEmail";
    private static final String EMAIL_TEMPLATE_PARAM = "mailTemplate";


    private static final LazySoftReference<SmtpGatewayConfigurationDescriptionProvider> INSTANCE;
    private static final CompiledST DEFAULT_NOTIFICATION_TEMPLATE;

    static {
        INSTANCE = new LazySoftReference<>();
        DEFAULT_NOTIFICATION_TEMPLATE = callUnchecked(DefaultMailTemplate.NOTIFICATION);
    }

    private final CompiledST defaultHealthStatusTemplate;
    private final CompiledST defaultNewResourceTemplate;
    private final CompiledST defaultRemovedResourceTemplate;
    private final CompiledST defaultScaleOutTemplate;
    private final CompiledST defaultScaleInTemplate;
    private final CompiledST defaultMaxSizeTemplate;
    
    private SmtpGatewayConfigurationDescriptionProvider(){
        defaultHealthStatusTemplate  = callUnchecked(DefaultMailTemplate.HEALTH_STATUS);
        defaultNewResourceTemplate = callUnchecked(DefaultMailTemplate.NEW_RESOURCE);
        defaultRemovedResourceTemplate = callUnchecked(DefaultMailTemplate.REMOVED_RESOURCE);
        defaultScaleOutTemplate = callUnchecked(DefaultMailTemplate.SCALE_OUT);
        defaultScaleInTemplate = callUnchecked(DefaultMailTemplate.SCALE_IN);
        defaultMaxSizeTemplate = callUnchecked(DefaultMailTemplate.MAX_CLUSTER_SIZE_REACHED);
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
                .map(templateLocation -> callUnchecked(new TemplateResolver(metadata.getName(), templateLocation)))
                .orElse(DEFAULT_NOTIFICATION_TEMPLATE);
    }

    CompiledST parseHealthStatusTemplate(final Map<String, String> parameters) {
        return getValue(parameters, HEALTH_STATUS_TEMPLATE_PARAM, Function.identity())
                .map(templateLocation -> callUnchecked(new TemplateResolver("HealthStatus", templateLocation)))
                .orElse(defaultHealthStatusTemplate);
    }

    CompiledST parseNewResourceTemplate(final Map<String, String> parameters) {
        return getValue(parameters, NEW_RESOURCE_TEMPLATE_PARAM, Function.identity())
                .map(templateLocation -> callUnchecked(new TemplateResolver("NewResource", templateLocation)))
                .orElse(defaultNewResourceTemplate);
    }

    CompiledST parseRemovedResourceTemplate(final Map<String, String> parameters) {
        return getValue(parameters, REMOVED_RESOURCE_TEMPLATE_PARAM, Function.identity())
                .map(templateLocation -> callUnchecked(new TemplateResolver("RemovedResource", templateLocation)))
                .orElse(defaultRemovedResourceTemplate);
    }

    CompiledST parseScaleOutTemplate(final Map<String, String> parameters) {
        return getValue(parameters, SCALE_OUT_TEMPLATE_PARAM, Function.identity())
                .map(templateLocation -> callUnchecked(new TemplateResolver("ScaleOut", templateLocation)))
                .orElse(defaultScaleOutTemplate);
    }

    CompiledST parseScaleInTemplate(final Map<String, String> parameters) {
        return getValue(parameters, SCALE_IN_TEMPLATE_PARAM, Function.identity())
                .map(templateLocation -> callUnchecked(new TemplateResolver("ScaleIn", templateLocation)))
                .orElse(defaultScaleInTemplate);
    }

    CompiledST parseMaxClusterSizeReachedTemplate(final Map<String, String> parameters) {
        return getValue(parameters, MAX_CLUSTER_SIZE_TEMPLATE_PARAM, Function.identity())
                .map(templateLocation -> callUnchecked(new TemplateResolver("MaxClusterSizeReached", templateLocation)))
                .orElse(defaultMaxSizeTemplate);
    }
}
