package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenType;
import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.connectors.mda.MDAResourceConfigurationDescriptorProvider.parseType;

/**
 * Represents collection of {@link MDAAttributeAccessor}.
 * @param <M> Type of attributes in the repository.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class MDAAttributeRepository<M extends MDAAttributeAccessor> extends AbstractAttributeRepository<M> implements Closeable, SafeCloseable {
    private final TimeSpan expirationTime;

    /**
     * Provides access to timer that measures time of last write.
     */
    protected final AccessTimer lastWriteAccess;

    protected MDAAttributeRepository(final String resourceName,
                                     final Class<M> attributeMetadataType,
                                     final TimeSpan expirationTime,
                                     final AccessTimer accessTimer) {
        super(resourceName, attributeMetadataType);
        this.lastWriteAccess = Objects.requireNonNull(accessTimer);
        this.expirationTime = Objects.requireNonNull(expirationTime);
    }

    protected abstract M connectAttribute(final String attributeID,
                                          final OpenType<?> attributeType,
                                          final AttributeDescriptor descriptor) throws Exception;

    /**
     * Connects to the specified attribute.
     *
     * @param attributeID The id of the attribute.
     * @param descriptor  Attribute descriptor.
     * @return The description of the attribute; or {@literal null},
     * @throws Exception Internal connector error.
     */
    @Override
    protected final M connectAttribute(final String attributeID, final AttributeDescriptor descriptor) throws Exception {
        return connectAttribute(attributeID, parseType(descriptor), descriptor);
    }

    /**
     * Gets storage used to read/write attribute values received from external Agents.
     * @return The storage used to read/write attributes.
     */
    protected abstract ConcurrentMap<String, Object> getStorage();

    /**
     * Gets default value of the named storage slot.
     * @param storageName The name of the storage slot.
     * @return Default value of the storage slot.
     */
    protected abstract Object getDefaultValue(final String storageName);

    /**
     * Resets all values in the storage to default.
     */
    public final void reset(){
        for(final String storageName: getStorage().keySet())
            getStorage().put(storageName, getDefaultValue(storageName));
        lastWriteAccess.reset();
    }

    @Override
    protected final Object getAttribute(final M metadata) {
        if(lastWriteAccess.compareTo(expirationTime) > 0)
            throw new IllegalStateException("Attribute value is too old. Backend component must supply a fresh value");
        else
            return metadata.getValue(getStorage());
    }

    @Override
    protected final void setAttribute(final M attribute, final Object value) throws InvalidAttributeValueException {
        attribute.setValue(value, getStorage());
        lastWriteAccess.reset();
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

    @Override
    public void close() {
        removeAll(true);
    }
}
