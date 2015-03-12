package com.itworks.snamp.testing.adapters.snmp;

import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.OID;

import java.io.IOException;

/**
 * Represent factory providing instancing of SnmpClient Interface realization
 * @author Evgeniy Kirichenko
 */
public final class SnmpClientFactory {
    private SnmpClientFactory(){

    }

    public static SnmpClient createSnmpV2(final String address) throws IOException {
        return new SnmpV2Client(address);
    }

    public static SnmpClient createSnmpV3(final String engineID,
                                          final String address,
                                          final String username,
                                          final SecurityLevel security,
                                          final String password,
                                          final OID authenticationProtocol,
                                          final String privacyKey,
                                          final OID privacyProtocol) throws IOException {
        return new SnmpV3Client(engineID, address, username, security, authenticationProtocol, password, privacyProtocol, privacyKey);
    }
}
