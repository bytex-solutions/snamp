package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.AgentConfiguration.*;

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
    protected void applyTo(final EntityMap<? extends ManagedResourceConfiguration> baseline) {
        baseline.remove(getResourceName());
    }
}
