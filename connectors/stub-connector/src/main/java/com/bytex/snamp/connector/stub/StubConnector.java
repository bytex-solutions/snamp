package com.bytex.snamp.connector.stub;

import com.bytex.snamp.connector.ManagedResourceConnectorBean;
import com.bytex.snamp.connector.attributes.reflection.ManagementAttribute;

import java.beans.IntrospectionException;
import java.math.BigInteger;
import java.util.Random;

/**
 * Represents managed resource connector for tests.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class StubConnector extends ManagedResourceConnectorBean {
    private final Random random;

    StubConnector(final String resourceName) throws IntrospectionException {
        super(resourceName);
        random = new Random(0xEDB88320);
    }

    @ManagementAttribute(description = "Randomized integer value")
    public int getRandomInt(){
        return random.nextInt();
    }

    @ManagementAttribute(description = "Floating-point number with Gaussian distribution")
    public double getGaussian(){
        return random.nextGaussian() * 100D;
    }

    @ManagementAttribute(description = "Randomized big integer value")
    public BigInteger getRandomBigInteger(){
        return BigInteger.valueOf(1000 * random.nextInt(1000));
    }

    @ManagementAttribute(description = "Randomized boolean value")
    public boolean getRandomBoolean(){
        return random.nextBoolean();
    }

    @ManagementAttribute(description = "Randomized byte array")
    public byte[] getRandomBytes(){
        final byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return bytes;
    }
}
