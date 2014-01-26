package com.snamp.adapters;

import org.snmp4j.mp.SnmpConstants;

/**
 * Represent factory providing instancing of SnmpClient Interface realization
 * @author Evgeniy Kirichenko
 */
final public class SnmpClientFactory {
    private SnmpClientFactory(){};

    public static SnmpClient getSnmpV2(final String address)
    {
        return new ClientSnmpV2(address);
    }

    public static SnmpClient getSnmpV3(final String address, final String username, final String password)
    {
        return new ClientSnmpV3(address, username, password);
    }

}
