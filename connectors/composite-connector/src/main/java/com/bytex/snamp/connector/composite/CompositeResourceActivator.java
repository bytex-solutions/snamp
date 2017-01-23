package com.bytex.snamp.connector.composite;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.ManagedResourceActivator;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class CompositeResourceActivator extends ManagedResourceActivator<CompositeResourceConnector> {

    @SpecialUse(SpecialUse.Case.OSGi)
    public CompositeResourceActivator(){
        super(CompositeResourceActivator::newResourceConnector,
                configurationDescriptor(CompositeResourceConfigurationDescriptor::getInstance));
    }

    private static CompositeResourceConnector newResourceConnector(final String resourceName,
                                                                   final ManagedResourceInfo configuration,
                                                                   final DependencyManager dependencies) throws Exception{
        final CompositeResourceConfigurationDescriptor parser = CompositeResourceConfigurationDescriptor.getInstance();
        final CompositeResourceConnector result = new CompositeResourceConnector(resourceName, configuration, parser);
        result.update(configuration);
        return result;
    }
}
