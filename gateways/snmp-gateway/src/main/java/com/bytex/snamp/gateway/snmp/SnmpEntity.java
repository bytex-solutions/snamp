package com.bytex.snamp.gateway.snmp;

import org.snmp4j.smi.OID;

import javax.management.MBeanFeatureInfo;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
interface SnmpEntity<M extends MBeanFeatureInfo> {
    OID getID();
    M getMetadata();
    boolean equals(final M metadata);
}
