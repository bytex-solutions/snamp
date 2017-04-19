package com.bytex.snamp.supervision.def;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.supervision.SupervisorActivator;

/**
 * Represents activator for {@link DefaultSupervisor}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class DefaultSupervisorActivator extends SupervisorActivator<DefaultSupervisor> {

    @SpecialUse(SpecialUse.Case.OSGi)
    public DefaultSupervisorActivator() {
        super(DefaultSupervisorActivator::createSupervisor,
                requiredServices(DefaultSupervisor.class).require(ConfigurationManager.class),
                new SupportServiceManager<?, ?>[]{
                        configurationDescriptor(DefaultSupervisorConfigurationDescriptionProvider::new)
                });
    }

    private static DefaultSupervisor createSupervisor(final String groupName,
                                                      final DependencyManager dependencies){
        return new DefaultSupervisor(groupName,
                dependencies.getService(ConfigurationManager.class).orElseThrow(AssertionError::new));
    }
}
