package com.bytex.snamp.moa.services;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.moa.DataAnalyzer;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import com.google.common.collect.ImmutableMap;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import java.util.Map;

import static com.bytex.snamp.MapUtils.getValue;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class Activator extends AbstractServiceLibrary {
    private static final String HISTORY_SIZE_PARAM = "topologyAnalyzerHistorySize";
    private static final long DEFAULT_HISTORY_SIZE = 10_000L;

    private static abstract class AnalyticalServiceProvider<S extends DataAnalyzer, T extends S> extends ProvidedService<S, T>{
        AnalyticalServiceProvider(final Class<S> serviceType, final RequiredService<?>... dependencies){
            super(serviceType, dependencies, DataAnalyzer.class);
            super.dependencies.add(ConfigurationManager.class, Utils.getBundleContextOfObject(this));
        }

        @Nonnull
        abstract T activateService(final Map<String, Object> identity,
                                    final ImmutableMap<String, String> configuration);

        @Override
        @Nonnull
        protected final T activateService(final Map<String, Object> identity) throws Exception {
            final ConfigurationManager manager = dependencies.getService(ConfigurationManager.class)
                    .orElseThrow(AssertionError::new);
            final ImmutableMap<String, String> configuration = manager.transformConfiguration(ImmutableMap::copyOf);
            return activateService(identity, configuration);
        }
    }

    private static final class TopologyAnalyzerProvider extends AnalyticalServiceProvider<TopologyAnalyzer, DefaultTopologyAnalyzer> {
        TopologyAnalyzerProvider() {
            super(TopologyAnalyzer.class);
        }

        @Nonnull
        @Override
        DefaultTopologyAnalyzer activateService(final Map<String, Object> identity, final ImmutableMap<String, String> configuration) {
            final long historySize = getValue(configuration, HISTORY_SIZE_PARAM, Long::parseLong).orElse(DEFAULT_HISTORY_SIZE);
            return new DefaultTopologyAnalyzer(historySize);
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public Activator() {
        super(new TopologyAnalyzerProvider());
    }

    @Override
    protected void start(final BundleContext context, final DependencyManager bundleLevelDependencies) {
    }
}
