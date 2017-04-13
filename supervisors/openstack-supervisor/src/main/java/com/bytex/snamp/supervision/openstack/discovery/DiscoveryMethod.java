package com.bytex.snamp.supervision.openstack.discovery;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public enum DiscoveryMethod {
    /**
     * The node in the cluster must announce its instance through SNAMP service registered in Keystone.
     */
    MANUAL,

    /**
     * SNAMP automatically discover new nodes in the cluster using Senlin.
     */
    AUTO
}
