package com.bytex.snamp.adapters.snmp;

import org.snmp4j.smi.OID;

import javax.management.MBeanFeatureInfo;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
interface SnmpEntity<M extends MBeanFeatureInfo> {
    OID getID();
    M getMetadata();
    boolean equals(final M metadata);
}
