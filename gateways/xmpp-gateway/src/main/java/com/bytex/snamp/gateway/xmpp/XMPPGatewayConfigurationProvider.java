package com.bytex.snamp.gateway.xmpp;

import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.bytex.snamp.jmx.DescriptorUtils;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.java7.Java7HostnameVerifier;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import javax.management.Descriptor;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Map;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.MapUtils.getValueAsInt;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class XMPPGatewayConfigurationProvider extends ConfigurationEntityDescriptionProviderImpl {
    private static final String USER_NAME_PARAM = "userName";
    private static final String PASSWORD_PARAM = "password";
    private static final String DOMAIN_PARAM = "domain";
    private static final String PORT_PARAM = "port";
    private static final String HOST_PARAM = "host";
    private static final String CONNECTION_TIMEOUT_PARAM = "connectionTimeout";
    private static final String KEYSTORE_PATH_PARAM = "keystore";
    private static final String KEYSTORE_TYPE_PARAM = "keystoreType";
    private static final String KEYSTORE_PASSWORD_PARAM = "keystorePassword";
    private static final String ALLOW_CUSTOM_CERTIFICATE_PARAM = "allowUnsafeCertificate";
    private static final String M2M_FEATURES_PARAM = "enableM2M";

    private static final class GatewayConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<GatewayConfiguration>{
        private static final String RESOURCE_NAME = "GatewayParameters";

        private GatewayConfigurationDescriptor(){
            super(RESOURCE_NAME, GatewayConfiguration.class,
                    PORT_PARAM,
                    HOST_PARAM,
                    USER_NAME_PARAM,
                    DOMAIN_PARAM,
                    PASSWORD_PARAM,
                    KEYSTORE_PASSWORD_PARAM,
                    KEYSTORE_PATH_PARAM,
                    KEYSTORE_TYPE_PARAM,
                    ALLOW_CUSTOM_CERTIFICATE_PARAM,
                    M2M_FEATURES_PARAM);
        }
    }

    XMPPGatewayConfigurationProvider(){
        super(new GatewayConfigurationDescriptor());
    }

    public static String getDomain(final Map<String, String> parameters){
        return parameters.get(DOMAIN_PARAM);
    }

    public static AbstractXMPPConnection createConnection(final Map<String, String> parameters) throws AbsentXMPPConfigurationParameterException, GeneralSecurityException, IOException {
        final String userName = getValue(parameters, USER_NAME_PARAM, Function.identity()).orElseThrow(() -> new AbsentXMPPConfigurationParameterException(USER_NAME_PARAM));
        final String password = getValue(parameters, PASSWORD_PARAM, Function.identity()).orElseThrow(() -> new AbsentXMPPConfigurationParameterException(PASSWORD_PARAM));
        final String host = getValue(parameters, HOST_PARAM, Function.identity()).orElseThrow(() -> new AbsentXMPPConfigurationParameterException(HOST_PARAM));
        final String domain = getValue(parameters, DOMAIN_PARAM, Function.identity()).orElseThrow(() -> new AbsentXMPPConfigurationParameterException(DOMAIN_PARAM));
        final int port = getValueAsInt(parameters, PORT_PARAM, Integer::parseInt).orElse(5222);
        final int connectionTimeout = getValueAsInt(parameters, CONNECTION_TIMEOUT_PARAM, Integer::parseInt).orElse(7000);
        final XMPPTCPConnectionConfiguration.Builder builder =
                XMPPTCPConnectionConfiguration.builder()
                        .setUsernameAndPassword(userName, password)
                        .setResource("Work")
                        .setServiceName(domain)
                        .setPort(port)
                        .setConnectTimeout(connectionTimeout)
                        .setSendPresence(true)
                        .setHost(host);
        //is security enabled?
        if(parameters.containsKey(KEYSTORE_PASSWORD_PARAM) ||
                parameters.containsKey(KEYSTORE_PATH_PARAM) ||
                parameters.containsKey(KEYSTORE_TYPE_PARAM)){
            builder.setSecurityMode(ConnectionConfiguration.SecurityMode.required);
            final String keystorePassword = parameters.get(KEYSTORE_PASSWORD_PARAM);
            final String keystoreType = parameters.get(KEYSTORE_TYPE_PARAM);
            final String keystorePath = parameters.get(KEYSTORE_PATH_PARAM);
            if(parameters.containsKey(ALLOW_CUSTOM_CERTIFICATE_PARAM) &&
                    !isNullOrEmpty(keystorePassword) && !isNullOrEmpty(keystorePath))
                builder.setCustomSSLContext(createSSLContext(keystorePath, keystoreType, keystorePassword))
                        .setHostnameVerifier(FakeTrustManagerFactory.FAKE_HOST_VERIFIER);
            else {
                builder.setKeystorePath(isNullOrEmpty(keystorePath) ? System.getProperty("javax.net.ssl.keyStore") : keystorePath)
                        .setKeystoreType(isNullOrEmpty(keystoreType) ? KeyStore.getDefaultType() : keystoreType)
                        .setCustomSSLContext(SSLContext.getDefault())
                        .setHostnameVerifier(new Java7HostnameVerifier());
            }
        }
        else builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setHostnameVerifier(FakeTrustManagerFactory.FAKE_HOST_VERIFIER);
        return new XMPPTCPConnection(builder.build());
    }

    //https://github.com/Flowdalic/java-pinning
    private static SSLContext createSSLContext(final String keystorePath,
                                               String keystoreType,
                                               final String keystorePassword) throws GeneralSecurityException, IOException {
        if(keystoreType == null || keystoreType.isEmpty())
            keystoreType = KeyStore.getDefaultType();
        // Create keystore
        final KeyStore ks = KeyStore.getInstance(keystoreType);
        try(final InputStream certificate = new FileInputStream(keystorePath)) {
            ks.load(certificate, keystorePassword.toCharArray());
        }

        // Set up key manager factory to use our key store
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, keystorePassword.toCharArray());

        // Initialize the SSLContext to work with our key managers.
        SSLContext sslContext = SSLContext.getInstance("TLS");
        final FakeTrustManagerFactory trustManager = new FakeTrustManagerFactory();
        sslContext.init(kmf.getKeyManagers(), trustManager.engineGetTrustManagers(), null);

        return sslContext;
    }

    static boolean isM2MEnabled(final Descriptor descr){
        return DescriptorUtils.hasField(descr, M2M_FEATURES_PARAM);
    }
}
