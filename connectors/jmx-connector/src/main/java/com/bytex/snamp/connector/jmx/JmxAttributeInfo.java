package com.bytex.snamp.connector.jmx;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenType;
import java.util.function.Function;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Represents an abstract class for building JMX attribute providers.
 */
final class JmxAttributeInfo extends OpenMBeanAttributeInfoSupport implements JmxAttributeMetadata {
    private static final long serialVersionUID = 3262046901190396737L;
    private final ObjectName namespace;
    private final AttributeDescriptor descriptor;

    private JmxAttributeInfo(final String attributeName,
                             final MBeanAttributeInfo nativeAttr,
                             final boolean isWritable,
                             final ObjectName namespace,
                             AttributeDescriptor metadata,
                             final Function<MBeanAttributeInfo, OpenType<?>> typeResolver) throws OpenDataException {
        super(attributeName,
                metadata.getDescription(nativeAttr.getDescription()),
                detectAttributeType(nativeAttr, typeResolver),
                nativeAttr.isReadable(),
                isWritable,
                nativeAttr.isIs(),
                metadata = metadata.setFields(nativeAttr.getDescriptor()));
        this.namespace = namespace;
        this.descriptor = metadata;
    }

    JmxAttributeInfo(final String attributeName,
                     final MBeanAttributeInfo nativeAttr,
                     final ObjectName namespace,
                     final AttributeDescriptor metadata) throws OpenDataException{
        this(attributeName,
                nativeAttr,
                nativeAttr.isWritable(),
                namespace,
                metadata,
                AttributeDescriptor::getOpenType);
    }

    private static OpenType<?> detectAttributeType(final MBeanAttributeInfo nativeAttr,
                                                   final Function<MBeanAttributeInfo, OpenType<?>> resolver) throws OpenDataException {
        final OpenType<?> result = resolver.apply(nativeAttr);
        if (result == null)
            throw new OpenDataException(String.format("Attribute %s with type %s cannot be mapped to Open Type", nativeAttr.getName(), nativeAttr.getType()));
        else return result;
    }

    /**
     * Returns the descriptor for the feature.  Changing the returned value
     * will have no affect on the original descriptor.
     *
     * @return a descriptor that is either immutable or a copy of the original.
     * @since 1.6
     */
    @Override
    public AttributeDescriptor getDescriptor() {
        return firstNonNull(descriptor, AttributeDescriptor.EMPTY_DESCRIPTOR);
    }

    /**
     * Returns the attribute owner.
     * @return An owner of this attribute.
     */
    @Override
    public ObjectName getOwner(){
        return namespace;
    }

    @Override
    public String getAlias(){
        return AttributeDescriptor.getName(this);
    }

    private static Object getValue(final JmxConnectionManager connectionManager,
                                   final String attributeName,
                                   final ObjectName owner) throws Exception{
        return connectionManager.handleConnection(connection -> connection.getAttribute(owner, attributeName));
    }

    Object getValue(final JmxConnectionManager connectionManager) throws Exception {
        return getValue(connectionManager, getAlias(), namespace);
    }

    private static void setValue(final JmxConnectionManager connectionManager,
                                 final String attributeName,
                                 final ObjectName owner,
                                 final Object value) throws Exception{
        connectionManager.handleConnection(connection -> {
            connection.setAttribute(owner, new Attribute(attributeName, value));
            return null;
        });
    }

    void setValue(final JmxConnectionManager connectionManager, final Object value) throws Exception {
        setValue(connectionManager, getAlias(), namespace, value);
    }
}
