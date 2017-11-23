package com.bytex.snamp.connector.jmx;

import com.bytex.snamp.connector.operations.OperationDescriptor;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
abstract class JmxOperationInfo extends MBeanOperationInfo implements JmxOperationMetadata {
    private static final long serialVersionUID = 3762430398491875048L;
    private final OperationDescriptor descriptor;

    JmxOperationInfo(final String name,
                             final String description,
                             final MBeanParameterInfo[] signature,
                             final String type,
                             final int impact,
                             final OperationDescriptor descriptor) {
        super(name, descriptor.getDescription(description), signature, type, impact, descriptor);
        this.descriptor = descriptor;
    }

    @Override
    public final String getAlias() {
        return OperationDescriptor.getName(this);
    }

    @Override
    public final OperationDescriptor getDescriptor() {
        return firstNonNull(descriptor, OperationDescriptor.EMPTY_DESCRIPTOR);
    }

    abstract Object invoke(final JmxConnectionManager connectionManager, final Object[] arguments) throws Exception;
}
