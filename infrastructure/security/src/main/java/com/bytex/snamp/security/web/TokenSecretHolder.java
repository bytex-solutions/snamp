package com.bytex.snamp.security.web;

import com.bytex.snamp.concurrent.LazyStrongReference;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.Utils;
import org.osgi.framework.BundleContext;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.function.Supplier;

/**
 * Provides access to secret used to verify and sign JWT token.
 */
final class TokenSecretHolder extends SecureRandom implements Supplier<BigInteger> {
    private static final String JWT_SECRET_BOX_NAME = "JWT_SECRET";
    private static final long serialVersionUID = 2764002554365647124L;
    private static final LazyStrongReference<TokenSecretHolder> INSTANCE = new LazyStrongReference<>();

    private TokenSecretHolder(){
    }

    /**
     * Generates a new secret.
     * @return Newly generated secret.
     */
    @Override
    public BigInteger get() {
        return new BigInteger(130, this);
    }

    static TokenSecretHolder getInstance() {
        return INSTANCE.lazyGet(TokenSecretHolder::new);
    }

    String getSecret(final BundleContext context) {
        return String.valueOf(DistributedServices.getDistributedBox(context, JWT_SECRET_BOX_NAME).setIfAbsent(this));
    }
}
