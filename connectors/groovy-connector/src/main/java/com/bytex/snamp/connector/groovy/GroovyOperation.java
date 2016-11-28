package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.jmx.OpenMBeanParameterInfoSupplier;
import groovy.lang.Closure;

import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenType;
import java.util.Collection;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class GroovyOperation extends OpenMBeanOperationInfoSupport {
    private static final long serialVersionUID = 2831645638008613535L;
    private final Closure<?> operation;

    GroovyOperation(final String name,
                           final String description,
                           final Collection<OpenMBeanParameterInfoSupplier<?>> signature,
                           final OpenType<?> returnOpenType,
                           final int impact,
                            final Closure<?> operation,
                           final OperationDescriptor descriptor) {
        super(name, descriptor.getDescription(description), OpenMBeanParameterInfoSupplier.toParameters(signature), returnOpenType, impact, descriptor);
        this.operation = Objects.requireNonNull(operation);
    }

    Object invoke(final Object... args){
        return operation.call(args);
    }
}
