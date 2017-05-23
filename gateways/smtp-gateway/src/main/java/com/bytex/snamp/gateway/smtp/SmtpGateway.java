package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.MapUtils;
import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.modeling.FeatureAccessor;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.management.MBeanFeatureInfo;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class SmtpGateway extends AbstractGateway {
    private Session mailSession;

    SmtpGateway(final String instanceName) {
        super(instanceName);
    }

    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName, final M feature) throws Exception {
        return null;
    }

    @Override
    protected Stream<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) throws Exception {
        return null;
    }

    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName, final M feature) throws Exception {
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

    @Override
    protected void start(final Map<String, String> parameters) throws SmtpGatewayAbsentConfigurationParameterException {
        final SmtpGatewayConfigurationDescriptionProvider parser = SmtpGatewayConfigurationDescriptionProvider.getInstance();
        mailSession = Session.getInstance(MapUtils.toProperties(parameters), createAuthenticator(parser.parseCredentials(parameters)));
    }

    @Override
    protected void stop() {
        mailSession = null;
    }
}
