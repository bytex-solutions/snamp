package com.bytex.snamp.connector.composite;

import com.bytex.snamp.Box;
import com.bytex.snamp.connector.operations.AbstractOperationRepository;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.connector.operations.OperationSupport;

import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents composition of operations.
 */
final class OperationComposition extends AbstractOperationRepository<CompositeOperation> {
    private final OperationSupportProvider provider;
    private final Logger logger;

    OperationComposition(final String resourceName,
                         final OperationSupportProvider provider,
                         final Logger logger){
        super(resourceName, CompositeOperation.class, false);
        this.provider = Objects.requireNonNull(provider);
        this.logger = Objects.requireNonNull(logger);
    }

    @Override
    protected CompositeOperation connectOperation(final String operationName, final OperationDescriptor descriptor) throws ReflectionException, MBeanException {
        final Box<String> connectorType = new Box<>();
        final Box<String> shortName = new Box<>();
        if(ConnectorTypeSplit.split(operationName, connectorType, shortName)){
            final OperationSupport support = provider.getOperationSupport(connectorType.get());
            if (support == null)
                throw operationsNotSupported(connectorType);
            final MBeanOperationInfo underlyingOperation = support.enableOperation(shortName.get(), descriptor);
            if (underlyingOperation == null)
                throw new ReflectionException(new IllegalStateException(String.format("Connector '%s' could not enable operation '%s'", connectorType, shortName)));
            return new CompositeOperation(operationName, underlyingOperation);
        } else
            throw new MBeanException(CompositeOperation.invalidName(operationName));
    }

    private static ReflectionException operationsNotSupported(final Object connectorType){
        return new ReflectionException(new UnsupportedOperationException(String.format("Connector '%s' doesn't support operations", connectorType)));
    }

    @Override
    protected void failedToEnableOperation(final String operationName, final Exception e) {
        failedToEnableOperation(logger, Level.WARNING, operationName, e);
    }


    @Override
    protected Object invoke(final OperationCallInfo<CompositeOperation> callInfo) throws Exception {
        final OperationSupport support = provider.getOperationSupport(callInfo.getMetadata().getConnectorType());
        if (support == null)
            throw operationsNotSupported(callInfo.getMetadata().getConnectorType());
        return support.invoke(callInfo.getMetadata().getShortName(), callInfo.toArray(), callInfo.getSignature());
    }

    @Override
    protected void disconnectOperation(final CompositeOperation metadata) {
        final OperationSupport support = provider.getOperationSupport(metadata.getConnectorType());
        if(support != null)
            support.removeOperation(metadata.getShortName());
    }
}
