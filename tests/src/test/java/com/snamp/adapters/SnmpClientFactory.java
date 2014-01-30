package com.snamp.adapters;

/**
 * Represent factory providing instancing of SnmpClient Interface realization
 * @author Evgeniy Kirichenko
 */
final public class SnmpClientFactory {
    private SnmpClientFactory(){};

    public static SnmpClient getSnmpV2(final String address)
    {
        return new SnmpV2Client(address);
    }

    public static SnmpClient getSnmpV3(final String address, final String username, final String password)
    {
        return new SnmpV3Client(address, username, password);
    }

}
