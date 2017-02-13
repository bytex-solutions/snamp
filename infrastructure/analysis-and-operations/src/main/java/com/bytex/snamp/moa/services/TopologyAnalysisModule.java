package com.bytex.snamp.moa.services;

import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;
import com.bytex.snamp.instrumentation.measurements.Span;
import com.bytex.snamp.moa.topology.GraphOfComponents;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents filtered graph of components.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
final class TopologyAnalysisModule extends GraphOfComponents {
    private static final long serialVersionUID = -2367174795240931165L;
    private final ConcurrentResourceAccessor<Map<String, Long>> allowedComponents;//key - component name, value - number of components with the same name

    TopologyAnalysisModule(long historySize) {
        super(historySize);
        allowedComponents = new ConcurrentResourceAccessor<>(new HashMap<>(20));
    }

    private static boolean filterSpan(final Map<String, ?> allowedComponents, final Span span) {
        return allowedComponents.containsKey(span.getComponentName());
    }

    @Override
    protected boolean filterSpan(final Span span) {
        return allowedComponents.read(components -> filterSpan(components, span));
    }

    private static Void add(final Map<String, Long> allowedComponents, final String componentName) {
        allowedComponents.compute(componentName, (k, v) -> v == null ? 0L : v + 1L);
        return null;
    }

    void add(final String componentName) {
        allowedComponents.write(components -> add(components, componentName));
    }

    private static Void remove(final Map<String, Long> allowedComponents, final String componentName) {
        allowedComponents.compute(componentName, (k, v) -> {
            if (v == null)
                return null;
            v = v - 1L;
            return v <= 1L ? null : v;
        });
        return null;
    }

    @Override
    public boolean remove(final String componentName) {
        allowedComponents.write(components -> remove(components, componentName));
        return super.remove(componentName);
    }
}
