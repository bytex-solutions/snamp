package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.connector.operations.AbstractOperationRepository;
import com.bytex.snamp.connector.operations.OperationDescriptor;

import javax.management.OperationsException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents repository of Groovy-based operations.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class GroovyOperationRepository extends AbstractOperationRepository<GroovyOperation> {
    private final ManagedResourceScriptlet scriptlet;

    GroovyOperationRepository(final String resourceName, final ManagedResourceScriptlet scriptlet){
        super(resourceName, GroovyOperation.class);
        this.scriptlet = Objects.requireNonNull(scriptlet);
    }

    @Override
    public Map<String, OperationDescriptor> discoverOperations() {
        final Map<String, OperationDescriptor> result = new HashMap<>();
        for(final String operationName: scriptlet.getOperations())
            result.put(operationName, createDescriptor());
        return result;
    }

    @Override
    protected GroovyOperation connectOperation(final String operationName, final OperationDescriptor descriptor) throws OperationsException {
        return scriptlet.createOperation(operationName, descriptor);
    }

    @Override
    protected Object invoke(final OperationCallInfo<GroovyOperation> callInfo) {
        return callInfo.getOperation().invoke(callInfo.toArray());
    }
}
