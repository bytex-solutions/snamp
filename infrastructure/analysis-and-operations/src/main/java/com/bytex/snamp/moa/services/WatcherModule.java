package com.bytex.snamp.moa.services;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.gateway.modeling.ModelOfAttributes;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import javax.management.MBeanAttributeInfo;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class WatcherModule extends ModelOfAttributes<AttributeWatcher> {
    private final Multimap<String, String> componentToResourceMap;

    WatcherModule(){
        componentToResourceMap = HashMultimap.create();
    }

    @Override
    protected AttributeWatcher createAccessor(final String resourceName, final MBeanAttributeInfo metadata) throws Exception {
        return new AttributeWatcher(metadata);
    }

    /**
     * Removes all attributes from this model.
     */
    @Override
    public void clear() {
        super.clear();
        componentToResourceMap.clear();
    }

    void addResource(final ManagedResourceConnectorClient resource){
        componentToResourceMap.put(resource.getComponentName(), resource.getManagedResourceName());
    }

    void removeResource(final ManagedResourceConnectorClient resource) {
        componentToResourceMap.remove(resource.getComponentName(), resource.getManagedResourceName());
    }
}
