package com.bytex.snamp.supervision.openstack;

import com.bytex.snamp.supervision.def.DefaultSupervisor;

/**
 * Represents supervisor for OpenStack.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class OpenStackSupervisor extends DefaultSupervisor {
    OpenStackSupervisor(final String groupName) {
        super(groupName);
    }
}
