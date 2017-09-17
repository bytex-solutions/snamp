package com.bytex.snamp.security.web;

import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.core.ClusterMember;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.function.Supplier;

/**
 * Provides access to secret used to verify and sign JWT token.
 */
final class TokenSecretHolder extends SecureRandom implements Supplier<BigInteger> {
    private static final String JWT_SECRET_BOX_NAME = "JWT_SECRET";
    private static final long serialVersionUID = 2764002554365647124L;
    private static final LazyReference<TokenSecretHolder> INSTANCE = LazyReference.strong();

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
        return INSTANCE.get(TokenSecretHolder::new);
    }

    String getSecret(final ClusterMember member) {
        return member.getBoxes()
                .getSharedObject(JWT_SECRET_BOX_NAME)
                .setIfAbsent(this)
                .toString();
    }
}
