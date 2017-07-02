package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.Notification;
import javax.management.openmbean.OpenType;
import java.io.Serializable;

/**
 * Represents attribute which state can be synchronized across cluster.
 * @param <T> Type of attribute value
 * @param <N> Type of notifications that can be handled by attribute.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class DistributedAttribute<T, N extends Notification> extends TypedSyntheticAttribute<N> {
    private static final long serialVersionUID = 1695985515176219703L;

    protected DistributedAttribute(final Class<N> notificationType,
                                   final String name,
                                   final OpenType<T> type,
                                   final String description,
                                   final AttributeDescriptor descriptor) throws InvalidSyntaxException {
        super(notificationType, name, type, description, AttributeSpecifier.READ_ONLY, descriptor);
    }

    protected abstract T getValue() throws Exception;

    protected abstract Serializable takeSnapshot();

    protected abstract void loadFromSnapshot(final Serializable snapshot);
}
