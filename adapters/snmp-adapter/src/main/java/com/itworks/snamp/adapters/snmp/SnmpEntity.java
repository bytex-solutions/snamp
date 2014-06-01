package com.itworks.snamp.adapters.snmp;

import org.snmp4j.smi.OID;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface SnmpEntity {
    OID getID();
}
