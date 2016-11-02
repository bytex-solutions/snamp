package com.bytex.snamp.webconsole;

import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.Utils;
import org.osgi.framework.BundleContext;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Provides access to secret used to verify and sign JWT token.
 */
final class TokenSecretHolder {
    private static final String JWT_SECRET_BOX_NAME = "JWT_SECRET";
    private static final SecureRandom RANDOM = new SecureRandom();

    private TokenSecretHolder(){
        throw new InstantiationError();
    }

    static String getSecret(final Object caller) {
        return String.valueOf(
                DistributedServices.getDistributedBox(Utils.getBundleContextOfObject(caller), JWT_SECRET_BOX_NAME)
                        .setIfAbsent(() -> new BigInteger(130, RANDOM))
        );
    }
}
