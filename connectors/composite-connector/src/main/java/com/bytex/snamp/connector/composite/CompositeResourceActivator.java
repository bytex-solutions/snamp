package com.bytex.snamp.connector.composite;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.core.ReplicationSupport;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class CompositeResourceActivator extends ManagedResourceActivator<CompositeResourceConnector> {
    private static final class CompositeResourceConnectorFactory implements ManagedResourceConnectorFactory<CompositeResourceConnector>{

        @Nonnull
        @Override
        public CompositeResourceConnector createConnector(final String resourceName, final ManagedResourceInfo configuration, final DependencyManager dependencies) throws Exception {
            final CompositeResourceConfigurationDescriptor parser = CompositeResourceConfigurationDescriptor.getInstance();
            final CompositeResourceConnector result = new CompositeResourceConnector(resourceName, configuration, parser);
            result.update(configuration);
            return result;
        }

        @Override
        public ImmutableSet<Class<? super CompositeResourceConnector>> getInterfaces() {
            return ImmutableSet.of(ReplicationSupport.class);
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public CompositeResourceActivator() {
        super(new CompositeResourceConnectorFactory(),
                configurationDescriptor(CompositeResourceConfigurationDescriptor::getInstance));
    }
}
