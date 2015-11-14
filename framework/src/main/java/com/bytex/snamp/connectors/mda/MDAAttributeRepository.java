package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.OpenAttributeRepository;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.jmx.DefaultValues;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.connectors.mda.MDAResourceConfigurationDescriptorProvider.parseType;

/**
 * Represents collection of {@link MDAAttributeInfo}.
 * @param <M> Type of attributes in the repository.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class MDAAttributeRepository<M extends MDAAttributeInfo> extends OpenAttributeRepository<M> implements SafeCloseable {
    private TimeSpan expirationTime;
    private AccessTimer lastWriteAccess;
    private final Cache<String, OpenType<?>> attributeTypes;

    /**
     * Initializes a new empty repository of attributes.
     * @param resourceName Name of the managed resource. Cannot be {@literal null} or empty.
     * @param attributeMetadataType Type of attributes in the repository. Cannot be {@literal null}.
     */
    protected MDAAttributeRepository(final String resourceName,
                                     final Class<M> attributeMetadataType) {
        super(resourceName, attributeMetadataType);
        attributeTypes = CacheBuilder.newBuilder().weakValues().build();
    }

    final void init(final TimeSpan expirationTime,
                    final AccessTimer accessTimer){
        this.expirationTime = expirationTime;
        this.lastWriteAccess = accessTimer;
    }

    /**
     * Connects attribute with this repository.
     * @param attributeID User-defined identifier of the attribute.
     * @param descriptor Metadata of the attribute.
     * @return Constructed attribute object.
     * @throws Exception Internal connector error.
     */
    protected abstract M createAttributeMetadata(final String attributeID,
                                                 final AttributeDescriptor descriptor) throws Exception;

    /**
     * Connects to the specified attribute.
     *
     * @param attributeID The id of the attribute.
     * @param descriptor  Attribute descriptor.
     * @return The description of the attribute; or {@literal null},
     * @throws Exception Internal connector error.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected final M connectAttribute(final String attributeID, final AttributeDescriptor descriptor) throws Exception {
        final OpenType<?> attributeType = parseType(descriptor);
        if(attributeType == null) throw new IllegalStateException("User-defined type of attribute is not supported");
        final M attribute = createAttributeMetadata(attributeID, descriptor.setOpenType(attributeType));
        attributeTypes.put(attribute.getStorageKey(), attributeType);
        attribute.init(lastWriteAccess, expirationTime, getStorage(), getDefaultValue(attributeType));
        return attribute;
    }

    /**
     * Gets storage used to read/write attribute values received from external Agents.
     * @return The storage used to read/write attributes.
     */
    protected ConcurrentMap<String, Object> getStorage(){
        return DistributedServices.getProcessLocalStorage(getResourceName());
    }

    /**
     * Gets default value of the named storage slot.
     * @param attributeType The name of the storage slot.
     * @return Default value of the storage slot.
     */
    @SuppressWarnings("unchecked")
    protected <T> T getDefaultValue(final OpenType<T> attributeType) throws OpenDataException{
        return attributeType instanceof CompositeType ?
                (T)DefaultValues.get((CompositeType)attributeType) :
                DefaultValues.get(attributeType);
    }

    /**
     * Gets type of the attribute.
     * @param storageKey The key in the storage.
     * @return Type of the attribute.
     */
    protected final OpenType<?> getAttributeType(final String storageKey){
        return attributeTypes.getIfPresent(storageKey);
    }

    /**
     * Resets last access time of all attributes.
     */
    protected final void resetAccessTime(){
        lastWriteAccess.reset();
    }

    /**
     * Resets all attributes in the storage to default.
     */
    @SuppressWarnings("unchecked")
    public final void reset() throws OpenDataException, InvalidAttributeValueException {
        for(final M attribute: getAttributeInfo())
            attribute.setValue(getDefaultValue(attribute.getOpenType()));
        resetAccessTime();
    }

    protected abstract Logger getLogger();

    @Override
    protected final void failedToConnectAttribute(final String attributeID, final String attributeName, final Exception e) {
        failedToConnectAttribute(getLogger(), Level.WARNING, attributeID, attributeName, e);
    }

    @Override
    protected final void failedToGetAttribute(final String attributeID, final Exception e) {
        failedToGetAttribute(getLogger(), Level.SEVERE, attributeID, e);
    }

    @Override
    protected final void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
        failedToSetAttribute(getLogger(), Level.SEVERE, attributeID, value, e);
    }

    /**
     * Removes all attributes from this repository.
     */
    @Override
    public void close() {
        removeAll(true);
        expirationTime = null;
        lastWriteAccess = null;
        attributeTypes.invalidateAll();
    }
}
