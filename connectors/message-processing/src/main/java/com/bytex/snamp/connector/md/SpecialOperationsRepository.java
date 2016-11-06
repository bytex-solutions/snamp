package com.bytex.snamp.connector.md;

import com.bytex.snamp.connector.operations.AbstractOperationRepository;
import com.bytex.snamp.connector.operations.OperationDescriptor;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents repository with special operations.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class SpecialOperationsRepository extends AbstractOperationRepository<SpecialOperation> {
    private final Logger logger;
    private final AttributeLookup attributes;

    SpecialOperationsRepository(final String resourceName, final AttributeLookup attributes, final Logger logger){
        super(resourceName, SpecialOperation.class, false);
        this.logger = Objects.requireNonNull(logger);
        this.attributes = Objects.requireNonNull(attributes);
    }

    @Override
    protected SpecialOperation connectOperation(final String operationName, final OperationDescriptor descriptor) {
        switch (descriptor.getName(operationName)){
            case ResetAllMetricsOperation.NAME:
                return new ResetAllMetricsOperation(operationName, descriptor);
            case ResetMetricAttribute.NAME:
                return new ResetMetricAttribute(operationName, descriptor);
            default:
                throw new IllegalArgumentException(String.format("Operation '%s' is not supported", operationName));
        }
    }


    @Override
    protected void failedToEnableOperation(final String operationName, final Exception e) {
        failedToEnableOperation(logger, Level.SEVERE, operationName, e);
    }

    /**
     * Invokes an operation.
     *
     * @param callInfo Operation call information. Cannot be {@literal null}.
     * @return Invocation result.
     * @throws Exception Unable to invoke operation.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Object invoke(final OperationCallInfo<SpecialOperation> callInfo) throws Exception {
        return callInfo.getOperation().invoke(attributes, callInfo.toNamedArguments());
    }
}
