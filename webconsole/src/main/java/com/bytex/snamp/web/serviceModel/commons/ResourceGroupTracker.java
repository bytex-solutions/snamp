package com.bytex.snamp.web.serviceModel.commons;

import com.bytex.snamp.concurrent.AbstractConcurrentResourceAccessor;
import com.bytex.snamp.concurrent.ConcurrentResourceAccessor;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.web.serviceModel.ManagedResourceTrackerSlim;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Used for tracking resources and collect them by groups.
 */
final class ResourceGroupTracker extends ManagedResourceTrackerSlim {
    //(componentType, resourceName)
    private final AbstractConcurrentResourceAccessor<Multimap<String, String>> resources = new ConcurrentResourceAccessor<>(HashMultimap.create());

    @Override
    protected void addResource(final ManagedResourceConnectorClient client) {
        final String groupName = client.getGroupName(), resourceName = client.getManagedResourceName();
        resources.write(resources -> resources.put(groupName, resourceName));
    }

    Set<String> getGroups() {
        return resources.read(resources -> ImmutableSet.copyOf(resources.keySet()));
    }

    @Override
    protected void removeResource(final ManagedResourceConnectorClient client) {
        final String groupName = client.getGroupName(), resourceName = client.getManagedResourceName();
        resources.write(resources -> resources.remove(groupName, resourceName));
    }

    @Override
    protected void stop() {
        resources.write(resources -> {
            resources.clear();
            return null;
        });
    }

    @Override
    protected void start(final Map<String, String> configuration) {

    }

    ImmutableSet<String> getResources(final String groupName) {
        return resources.read(resources -> ImmutableSet.copyOf(isNullOrEmpty(groupName) ? resources.values() : resources.get(groupName)));
    }
}
