package com.bytex.snamp.supervision.openstack;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.supervision.SupervisorActivator;

import javax.annotation.Nonnull;

/**
 * Represents activator of {@link OpenStackSupervisor}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class OpenStackSupervisorActivator extends SupervisorActivator<OpenStackSupervisor> {
    @SpecialUse(SpecialUse.Case.OSGi)
    public OpenStackSupervisorActivator(){
        super(OpenStackSupervisorActivator::create);
    }

    @Nonnull
    private static OpenStackSupervisor create(final String groupName,
                                              final DependencyManager dependencies){
        return new OpenStackSupervisor(groupName);
    }
}
