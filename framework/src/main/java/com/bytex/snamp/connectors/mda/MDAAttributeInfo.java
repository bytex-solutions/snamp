package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.attributes.OpenMBeanAttributeAccessor;
import com.bytex.snamp.internal.MapKeyRef;
import com.bytex.snamp.jmx.OpenMBean;

import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents attribute that can be changed by remote Agent.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class MDAAttributeInfo<T> extends OpenMBeanAttributeAccessor<T> {
    private static final long serialVersionUID = -1853294450682902061L;

    private Map.Entry<String, Object> entryRef;
    private AccessTimer accessTimer;
    private TimeSpan expirationTime;
    private final OpenType<T> attributeType;

    public MDAAttributeInfo(final String name,
                               final OpenType<T> type,
                               final AttributeSpecifier specifier,
                               final AttributeDescriptor descriptor) {
        super(name, descriptor.getDescription(name), type, specifier, descriptor);
        this.attributeType = type;
    }

    public MDAAttributeInfo(final String name,
                            final OpenType<T> type,
                            final AttributeDescriptor descriptor){
        this(name, type, AttributeSpecifier.READ_WRITE, descriptor);
    }

    /**
     * Gets storage key used by this attribute to read/write its own value.
     * @return The storage key.
     */
    public final String getStorageKey(){
        return getDescriptor().getAttributeName();
    }

    final void init(final AccessTimer accessTimer, final TimeSpan expirationTime, final ConcurrentMap<String, Object> storage, final T initialValue){
        storage.put(getStorageKey(), initialValue);
        this.accessTimer = accessTimer;
        this.expirationTime = expirationTime;
        this.entryRef = new MapKeyRef<>(storage, getStorageKey());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final T getValue() throws OpenDataException {
        if(accessTimer.compareTo(expirationTime) > 0)
            throw new IllegalStateException(String.format("Attribute %s is not available because its value was expired", getName()));
        return OpenMBean.cast(attributeType, entryRef.getValue());
    }

    @Override
    protected final void setValue(final T value) throws InvalidAttributeValueException {
        try {
            entryRef.setValue(OpenMBean.cast(getOpenType(), value));
        } catch (final OpenDataException e) {
            throw new InvalidAttributeValueException(e.getMessage());
        }
        finally {
            accessTimer.reset();
        }
    }
}
