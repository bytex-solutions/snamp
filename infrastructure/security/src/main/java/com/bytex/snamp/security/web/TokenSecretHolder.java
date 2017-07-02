package com.bytex.snamp.security.web;

import com.bytex.snamp.concurrent.LazyStrongReference;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.SharedBox;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.function.Supplier;

import static com.bytex.snamp.core.SharedObjectType.BOX;

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

    String getSecret(final ClusterMember member) {
        final Optional<SharedBox> secretHolder = member.getService(JWT_SECRET_BOX_NAME, BOX);
        return String.valueOf(secretHolder.map(box -> box.setIfAbsent(this)).orElseGet(this::get));
    }
}
