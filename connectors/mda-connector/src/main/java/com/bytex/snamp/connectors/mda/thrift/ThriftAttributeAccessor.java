package com.bytex.snamp.connectors.mda.thrift;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.mda.MdaAttributeAccessor;

import javax.management.InvalidAttributeValueException;
import javax.management.openmbean.OpenType;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ThriftAttributeAccessor extends MdaAttributeAccessor {
    private final ThriftAttributeManager parser;

    protected ThriftAttributeAccessor(final String name,
                                      final OpenType<?> type,
                                      final AttributeSpecifier specifier,
                                      final AttributeDescriptor descriptor,
                                      final ThriftAttributeManager manager) {
        super(name, type, specifier, descriptor);
        this.parser = Objects.requireNonNull(manager);
    }

    @Override
    public final Object setValue(final Object value, final ConcurrentMap<String, Object> storage) throws InvalidAttributeValueException {
        return parser.setValue(value, storage);
    }

    @Override
    public final Object getValue(final ConcurrentMap<String, ?> storage) {
        return parser.getValue(storage);
    }
}
