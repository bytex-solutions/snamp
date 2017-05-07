package com.bytex.snamp.connector.stub;

import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceConnectorBean;
import com.bytex.snamp.connector.attributes.reflection.ManagementAttribute;

import java.beans.IntrospectionException;
import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents managed resource connector for tests.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class StubConnector extends ManagedResourceConnectorBean {
    private final Random random;
    private int intValue;
    private final AtomicInteger staggering;

    StubConnector(final String resourceName, final ManagedResourceInfo configuration) throws IntrospectionException {
        super(resourceName);
        random = new Random(0xEDB88320);
        staggering = new AtomicInteger(-20);
        setConfiguration(configuration);
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

    @ManagementAttribute(description = "Writable integer attribute")
    public int getIntValue(){
        return intValue;
    }

    public void setIntValue(final int value){
        intValue = value;
    }

    @ManagementAttribute(description = "Gets staggering value in range [-20, 20]")
    public int getStaggeringValue() {
        int prev, next;
        do {
            prev = staggering.get();
            next = prev + 1;
            if (next > 20)
                next = -20;
        } while (!staggering.compareAndSet(prev, next));
        return next;
    }
}
