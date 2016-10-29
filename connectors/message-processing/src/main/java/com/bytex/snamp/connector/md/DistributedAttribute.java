package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;

import javax.management.openmbean.OpenType;
import java.io.Serializable;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class DistributedAttribute<T> extends MessageDrivenAttribute {
    private static final long serialVersionUID = 1695985515176219703L;

    protected DistributedAttribute(final String name,
                                   final OpenType<T> type,
                                   final String description,
                                   final AttributeDescriptor descriptor) {
        super(name, type, description, AttributeSpecifier.READ_ONLY, descriptor);
    }

    protected abstract T getValue() throws Exception;

    protected abstract Serializable takeSnapshot();

    protected abstract void loadFromSnapshot(final Serializable snapshot);
}
