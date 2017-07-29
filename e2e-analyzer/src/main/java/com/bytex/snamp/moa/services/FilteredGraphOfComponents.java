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
 * @version 2.1
 */
final class FilteredGraphOfComponents extends GraphOfComponents {
    private static final long serialVersionUID = -2367174795240931165L;
    private final ConcurrentResourceAccessor<Map<String, Long>> allowedGroups;//key - component name, value - number of components with the same name

    FilteredGraphOfComponents(final long historySize) {
        super(historySize);
        allowedGroups = new ConcurrentResourceAccessor<>(new HashMap<>(20));
    }

    private static boolean filterSpan(final Map<String, ?> allowedGroups, final Span span) {
        return allowedGroups.containsKey(span.getComponentName());
    }

    @Override
    protected boolean filterSpan(final Span span) {
        return allowedGroups.read(groups -> filterSpan(groups, span));
    }

    private static Long add(final Map<String, Long> allowedGroups, final String groupName) {
        return allowedGroups.compute(groupName, (k, v) -> v == null ? 0L : v + 1L);
    }

    void add(final String groupName) {
        allowedGroups.write(groups -> add(groups, groupName));
    }

    private static Long remove(final Map<String, Long> allowedGroups, final String groupName) {
        return allowedGroups.compute(groupName, (k, v) -> {
            if (v == null)
                return null;
            v = v - 1L;
            return v <= 1L ? null : v;
        });
    }

    @Override
    public boolean remove(final String groupName) {
        allowedGroups.write(components -> remove(components, groupName));
        return super.remove(groupName);
    }
}
