package com.bytex.snamp.connectors.mda.impl;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.mda.MDAAttributeInfo;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.ObjectStorage;
import com.bytex.snamp.internal.Utils;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class MDAAttributeRepository extends com.bytex.snamp.connectors.mda.MDAAttributeRepository<MDAAttributeInfo> {
    private final Logger logger;
    private final ObjectStorage storageService;

    public MDAAttributeRepository(final String resourceName,
                                    final Logger logger) {
        super(resourceName, MDAAttributeInfo.class);
        this.logger = Objects.requireNonNull(logger);
        this.storageService = DistributedServices.getDistributedObjectStorage(Utils.getBundleContextByObject(this));
    }

    private String getCollectionName(){
        return "attributes-".concat(getResourceName());
    }

    /**
     * Connects attribute with this repository.
     *
     * @param attributeID User-defined identifier of the attribute.
     * @param descriptor  Metadata of the attribute.
     * @return Constructed attribute object.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected final MDAAttributeInfo createAttributeMetadata(final String attributeID, final AttributeDescriptor descriptor) {
        return new MDAAttributeInfo(attributeID, descriptor.getOpenType(), descriptor);
    }

    @Override
    protected final ConcurrentMap<String, Object> getStorage() {
        return storageService.getCollection(getCollectionName());
    }

    @Override
    protected final Logger getLogger() {
        return logger;
    }

    @Override
    public void close() {
        super.close();
        storageService.deleteCollection(getCollectionName());
    }
}
