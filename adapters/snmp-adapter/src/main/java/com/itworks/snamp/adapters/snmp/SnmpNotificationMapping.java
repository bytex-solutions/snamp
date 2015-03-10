package com.itworks.snamp.adapters.snmp;

import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

import javax.management.MBeanNotificationInfo;

import static com.itworks.snamp.adapters.snmp.SnmpHelpers.DateTimeFormatter;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface SnmpNotificationMapping extends SnmpEntity {
    DateTimeFormatter getTimestampFormatter();

    OID getTransportDomain();

    OctetString getReceiverAddress();

    OctetString getReceiverName();

    int getTimeout();

    int getRetryCount();

    OID getID();

    MBeanNotificationInfo getMetadata();

    String getSource();
}
