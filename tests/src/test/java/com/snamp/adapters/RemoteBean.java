package com.snamp.adapters;

import java.math.BigInteger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class RemoteBean implements RemoteBeanInterface {
    private BigInteger bigInteger = BigInteger.ZERO;

    @Override
    public BigInteger getBigint() {
        return bigInteger;
    }

    @Override
    public void setBigint(final BigInteger value) {
        this.bigInteger = value;
    }
}
