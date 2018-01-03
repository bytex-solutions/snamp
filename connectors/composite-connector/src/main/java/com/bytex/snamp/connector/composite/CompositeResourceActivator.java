package com.bytex.snamp.connector.composite;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.core.ReplicationSupport;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.concurrent.ExecutorService;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class CompositeResourceActivator extends ManagedResourceActivator {


    private static final class CompositeResourceConnectorFactory extends ManagedResourceLifecycleManager<CompositeResourceConnector> {
        private CompositeResourceConnectorFactory() {
            super(ImmutableList.of(ReplicationSupport.class));
        }

        @Nonnull
        @Override
        protected CompositeResourceConnector createConnector(final String resourceName, final ManagedResourceConfiguration configuration) throws Exception {
            final CompositeResourceConfigurationDescriptor parser = CompositeResourceConfigurationDescriptor.getInstance();
            final ExecutorService threadPool = parser.parseThreadPool(configuration);
            final URL[] groovyPath = parser.parseGroovyPath(configuration);
            final CompositeResourceConnector result = new CompositeResourceConnector(resourceName, threadPool, groovyPath);
            result.setConnectionStringSeparator(parser.parseSeparator(configuration));
            updateConnector(result, configuration);
            return result;
        }

        @Override
        protected CompositeResourceConnector updateConnector(@Nonnull final CompositeResourceConnector connector, final ManagedResourceConfiguration configuration) throws Exception {
            connector.update(configuration.getConnectionString(), configuration);
            return connector;
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public CompositeResourceActivator() {
        super(new CompositeResourceConnectorFactory(),
                configurationDescriptor(CompositeResourceConfigurationDescriptor::getInstance));
    }
}
