package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.gateway.GatewayDescriptionProvider;

import javax.mail.PasswordAuthentication;
import java.util.Map;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.getValue;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SmtpGatewayConfigurationDescriptionProvider extends ConfigurationEntityDescriptionProviderImpl implements GatewayDescriptionProvider {
    private static final String USER_NAME_PARAM = "userName";
    private static final String PASSWORD_PARAM = "password";

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
}
