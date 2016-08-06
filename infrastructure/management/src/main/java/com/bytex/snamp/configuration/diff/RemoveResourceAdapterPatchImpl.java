package com.bytex.snamp.configuration.diff;

import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ResourceAdapterConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class RemoveResourceAdapterPatchImpl extends AbstractResourceAdapterInstancePatch implements RemoveResourceAdapterPatch {

    RemoveResourceAdapterPatchImpl(final String adapterInstanceName, final ResourceAdapterConfiguration adapter) {
        super(adapterInstanceName, adapter);
    }

    @Override
    protected void applyTo(final EntityMap<? extends ResourceAdapterConfiguration> adapters) {
        adapters.remove(getEntityID());
    }
}
