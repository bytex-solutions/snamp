package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class AddManagedResourcePatchImpl extends AbstractManagedResourcePatch implements AddManagedResourcePatch {

    AddManagedResourcePatchImpl(final String resourceName, final ManagedResourceConfiguration resource) {
        super(resourceName, resource);
    }

    @Override
    protected void applyTo(final Map<String, ManagedResourceConfiguration> baseline) {
        baseline.put(getResourceName(), getResource());
    }
}
