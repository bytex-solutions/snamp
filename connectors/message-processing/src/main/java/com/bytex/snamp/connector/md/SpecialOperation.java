package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.operations.AbstractOpenOperationInfo;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.jmx.OpenMBeanParameterInfoSupplier;

import javax.management.openmbean.OpenType;
import java.util.Map;

/**
 * Represents base class for special operation.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class SpecialOperation<T, R extends OpenType<T>> extends AbstractOpenOperationInfo<T, R> {
    private static final long serialVersionUID = -3814740217328271297L;

    SpecialOperation(final String name,
                            final String description,
                            final OpenMBeanParameterInfoSupplier<?>[] signature,
                            final R returnType,
                            final int impact,
                            final OperationDescriptor descriptor) {
        super(name, description, signature, returnType, impact, descriptor);
    }

    abstract T invoke(final AttributeLookup lookup, final Map<String, ?> arguments) throws Exception;
}
