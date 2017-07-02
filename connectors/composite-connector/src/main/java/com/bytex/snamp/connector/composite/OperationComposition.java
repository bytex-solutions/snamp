package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.operations.AbstractOperationRepository;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.connector.operations.OperationSupport;

import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import java.util.Objects;

/**
 * Represents composition of operations.
 */
final class OperationComposition extends AbstractOperationRepository<CompositeOperation> {
    private final OperationSupportProvider provider;

    OperationComposition(final String resourceName,
                         final OperationSupportProvider provider) {
        super(resourceName, CompositeOperation.class);
        this.provider = Objects.requireNonNull(provider);
    }

    @Override
    protected CompositeOperation connectOperation(final String operationName, final OperationDescriptor descriptor) throws ReflectionException, MBeanException, AbsentCompositeConfigurationParameterException {
        final String connectorType = CompositeResourceConfigurationDescriptor.parseSource(descriptor);
        final OperationSupport support = provider.getOperationSupport(connectorType)
                .orElseThrow(() -> operationsNotSupported(connectorType));
        final MBeanOperationInfo underlyingOperation = support.enableOperation(operationName, descriptor)
                .orElseThrow(() -> new ReflectionException(new IllegalStateException(String.format("Connector '%s' could not enable operation '%s'", connectorType, operationName))));
        return new CompositeOperation(connectorType, underlyingOperation);
    }

    private static ReflectionException operationsNotSupported(final Object connectorType) {
        return new ReflectionException(new UnsupportedOperationException(String.format("Connector '%s' doesn't support operations", connectorType)));
    }

    @Override
    protected Object invoke(final OperationCallInfo<CompositeOperation> callInfo) throws Exception {
        final OperationSupport support = provider.getOperationSupport(callInfo.getOperation().getConnectorType())
                .orElseThrow(() -> operationsNotSupported(callInfo.getOperation().getConnectorType()));
        return support.invoke(callInfo.getOperation().getName(), callInfo.toArray(), callInfo.getSignature());
    }

    @Override
    protected void disconnectOperation(final CompositeOperation metadata) {
        provider.getOperationSupport(metadata.getConnectorType())
                .ifPresent(support -> support.removeOperation(metadata.getName()));
    }
}
