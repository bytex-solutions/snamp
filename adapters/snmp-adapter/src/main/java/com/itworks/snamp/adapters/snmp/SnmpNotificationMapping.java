package com.itworks.snamp.adapters.snmp;

import org.snmp4j.agent.NotificationOriginator;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

import javax.management.MBeanNotificationInfo;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface SnmpNotificationMapping extends SnmpEntity<MBeanNotificationInfo> {

    OID getTransportDomain();

    OctetString getReceiverAddress();

    OctetString getReceiverName();

    int getTimeout();

    int getRetryCount();

    void setNotificationOriginator(final NotificationOriginator originator);

    void setTypeMapper(final SnmpTypeMapper value);
}
