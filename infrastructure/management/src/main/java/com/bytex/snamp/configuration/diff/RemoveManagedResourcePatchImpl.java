package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class RemoveManagedResourcePatchImpl extends AbstractManagedResourcePatch implements RemoveManagedResourcePatch {

    RemoveManagedResourcePatchImpl(final String resourceName, final ManagedResourceConfiguration resource) {
        super(resourceName, resource);
    }

    @Override
    protected void applyTo(final EntityMap<? extends ManagedResourceConfiguration> baseline) {
        baseline.remove(getEntityID());
    }
}
