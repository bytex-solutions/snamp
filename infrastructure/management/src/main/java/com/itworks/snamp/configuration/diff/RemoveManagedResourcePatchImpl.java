package com.itworks.snamp.configuration.diff;

import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class RemoveManagedResourcePatchImpl extends AbstractManagedResourcePatch implements RemoveManagedResourcePatch {

    RemoveManagedResourcePatchImpl(final String resourceName, final ManagedResourceConfiguration resource) {
        super(resourceName, resource);
    }

    @Override
    protected void applyTo(final Map<String, ManagedResourceConfiguration> baseline) {
        baseline.remove(getResourceName());
    }
}
