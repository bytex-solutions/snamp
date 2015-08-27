package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.attributes.OpenTypeAttributeInfo;

import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenType;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents abstract class for all attributes in MDA Connector.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class MdaAttributeAccessor extends OpenTypeAttributeInfo {

    protected MdaAttributeAccessor(final String name,
                                   final OpenType<?> type,
                                   final AttributeSpecifier specifier,
                                   final AttributeDescriptor descriptor) {
        super(name, type, descriptor.getDescription(name), specifier, descriptor);
    }

    public abstract Object setValue(final Object value, final ConcurrentMap<String, Object> storage) throws InvalidAttributeValueException;

    public abstract Object getValue(final ConcurrentMap<String, ?> storage);
}
