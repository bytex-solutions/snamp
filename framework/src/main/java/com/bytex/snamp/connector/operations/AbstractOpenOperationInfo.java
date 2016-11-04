package com.bytex.snamp.connector.operations;

import com.bytex.snamp.jmx.OpenMBeanParameterInfoSupplier;

import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenType;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Represents abstract class for OpenData-enabled JMX operation.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class AbstractOpenOperationInfo<T, R extends OpenType<T>> extends OpenMBeanOperationInfoSupport implements OperationDescriptorRead {
    private static final long serialVersionUID = 2796641341985745854L;
    private final OperationDescriptor descriptor;

    public AbstractOpenOperationInfo(final String name,
                                     final String description,
                                     final OpenMBeanParameterInfoSupplier<?>[] signature,
                                     final R returnType,
                                     final int impact,
                                     final OperationDescriptor descriptor) {
        super(name, description, OpenMBeanParameterInfoSupplier.toParameters(signature), returnType, impact, descriptor);
        this.descriptor = descriptor;
    }

    @Override
    public final OperationDescriptor getDescriptor() {
        return firstNonNull(descriptor, OperationDescriptor.EMPTY_DESCRIPTOR);
    }
}
