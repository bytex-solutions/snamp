package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;

import javax.management.openmbean.OpenType;
import java.io.Serializable;

/**
 * Represents attribute which state should be synchronized across cluster.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class StatefulAttribute extends MessageDrivenAttribute {
    private static final long serialVersionUID = -7486046895683300081L;

    interface Checkpoint extends Serializable{
        /**
         * Gets name of attribute which produces this checkpoint
         * @return Name of attribute which produces this checkpoint.
         */
        String getName();
    }

    StatefulAttribute(final String name,
                      final OpenType<?> type,
                      final String description,
                      final AttributeSpecifier specifier,
                      final AttributeDescriptor descriptor) {
        super(name, type, description, specifier, descriptor);
    }

    abstract Checkpoint createCheckpoint();

    abstract boolean loadCheckpoint(final Checkpoint state);
}
