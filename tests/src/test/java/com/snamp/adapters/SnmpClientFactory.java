package com.snamp.adapters;

import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.OID;

/**
 * Represent factory providing instancing of SnmpClient Interface realization
 * @author Evgeniy Kirichenko
 */
final public class SnmpClientFactory {
    private SnmpClientFactory(){};

    public static SnmpClient createSnmpV2(final String address){
        return new SnmpV2Client(address);
    }

    public static SnmpClient createSnmpV3(final String address, final String username, final SecurityLevel security){
        return new SnmpV3Client(address, username, security);
    }
}
