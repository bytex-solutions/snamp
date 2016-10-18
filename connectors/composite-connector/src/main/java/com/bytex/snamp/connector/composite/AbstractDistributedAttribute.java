package com.bytex.snamp.connector.composite;

import javax.management.*;
import javax.management.openmbean.OpenType;

/**
 * Created by Роман on 18.10.2016.
 */
public class AbstractDistributedAttribute<V> extends AbstractCompositeAttribute {
    private final OpenType<V> type;

    AbstractDistributedAttribute(final String name,
                                 final OpenType<V> type,
                                 final String description, boolean isReadable, boolean isWritable, boolean isIs, Descriptor descriptor) {
        super(name, type.getClassName(), description, isReadable, isWritable, isIs, descriptor);
        this.type = type;
    }

    @Override
    final V getValue(final AttributeSupportProvider provider) throws Exception {
        return null;
    }

    @Override
    void setValue(AttributeSupportProvider provider, Object value) throws AttributeNotFoundException, MBeanException, InvalidAttributeValueException, ReflectionException {

    }
}
