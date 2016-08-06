package com.bytex.snamp.adapters.xmpp;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.MethodStub;

import javax.net.ssl.*;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * Certificate Trust Manager for self-signed certificates.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class FakeTrustManagerFactory extends TrustManagerFactorySpi {
    static final HostnameVerifier FAKE_HOST_VERIFIER = (hostname, session) -> true;

    private static final X509TrustManager X509 = new X509TrustManager() {
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
        }

        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return ArrayUtils.emptyArray(X509Certificate[].class);
        }
    };

    private static final TrustManager[] X509_MANAGERS = new TrustManager[] { X509 };

    public FakeTrustManagerFactory() {
    }

    @Override
    public TrustManager[] engineGetTrustManagers() {
        return X509_MANAGERS;
    }

    @Override
    @MethodStub
    protected void engineInit(final KeyStore keystore){
    }

    @Override
    @MethodStub
    protected void engineInit(final ManagerFactoryParameters managerFactoryParameters){
    }


}

