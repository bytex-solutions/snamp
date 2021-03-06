package com.bytex.snamp.testing.gateway.snmp;

import org.snmp4j.PDU;
import org.snmp4j.Target;
import org.snmp4j.smi.OID;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.PDUFactory;

/**
 * Represents the set of available GET methods for snmp
 * @author Sakno Roman
 */
enum ReadMethod{
    GET(PDU.GET),
    GET_BULK(PDU.GETBULK);

    private final int method;

    ReadMethod(final int m){
        method = m;
    }

    int getPduType(){
        return method;
    }

    PDUFactory createPduFactory(){
        return new DefaultPDUFactory(method) {
            @Override
            public PDU createPDU(final Target target) {
                return createPDU(target, getPduType());
            }
        };
    }

    void prepareOIDs(final OID[] oids) {
        switch (method){
            case PDU.GETBULK:
                for(int i = 0; i < oids.length; i++)
                    oids[i] = prepareOidForGetBulk(oids[i]);
        }
    }

    private static OID prepareOidForGetBulk(final OID oid) {
        //if ends with '0' then remove it
        final String result = oid.toString();
        if(result.endsWith(".0"))
            return new OID(result.substring(0, result.length() - 2));
        else {
            final int lastDot = result.lastIndexOf('.');
            final int num = Integer.valueOf(result.substring(lastDot + 1)) + 1;
            return new OID(result.substring(0, lastDot) + "." + num);
        }
    }
}

