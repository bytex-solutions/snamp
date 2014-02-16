package com.snamp.adapters;

import org.snmp4j.PDU;
import org.snmp4j.smi.OID;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.PDUFactory;

/**
 * Represents the set of available GET methods for snmp
 * @author Sakno Roman
 */
public enum ReadMethod{
    GET(PDU.GET),
    GETBULK(PDU.GETBULK);

    private final int method;

    private ReadMethod(final int m){
        method = m;
    }

    public final void setPduType(final PDU pdu){
        pdu.setType(method);
    }

    public final int getPduType(){
        return method;
    }

    public final PDUFactory createPduFactory(){
        return new DefaultPDUFactory(method);
    }

    public final void prepareOIDs(final OID[] oids) {
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

