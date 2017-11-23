package com.bytex.snamp.connector.operations.reflection;

import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.operations.AbstractOperationRepository;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.google.common.collect.ImmutableList;

import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.beans.BeanInfo;
import java.beans.MethodDescriptor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents repository of attributes reflected from JavaBean methods.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@Deprecated
public abstract class JavaBeanOperationRepository extends AbstractOperationRepository<JavaBeanOperationInfo> {
    private Object owner;

    protected JavaBeanOperationRepository(final String resourceName, final Object owner) {
        super(resourceName, JavaBeanOperationInfo.class);
        this.owner = Objects.requireNonNull(owner);
    }

    protected abstract Collection<MethodDescriptor> getMethods();

    @Override
    protected JavaBeanOperationInfo connectOperation(final String operationName,
                                                     final OperationDescriptor descriptor) throws ReflectionException, MBeanException {
        for (final MethodDescriptor method : getMethods())
            if (Objects.equals(method.getName(), descriptor.getAlternativeName().orElse(operationName)) && JavaBeanOperationInfo.isValidDescriptor(method)) {
                return new JavaBeanOperationInfo(operationName, method, descriptor);
            }
        throw new MBeanException(new IllegalArgumentException(String.format("Operation '%s' doesn't exist", descriptor.getAlternativeName().orElse(operationName))));
    }

    private ClassLoader getClassLoader() {
        return owner.getClass().getClassLoader();
    }

    @Override
    public Map<String, OperationDescriptor> discoverOperations() {
        final Map<String, OperationDescriptor> result = new HashMap<>();
        for (final MethodDescriptor method : getMethods())
            if (JavaBeanOperationInfo.isValidDescriptor(method))
                result.put(method.getName(), createDescriptor());
        return result;
    }

    @Override
    protected Object invoke(final OperationCallInfo<JavaBeanOperationInfo> callInfo) throws Exception {
        return callInfo.getOperation().invoke(owner, callInfo.toArray());
    }

    /**
     * Removes all operations from this repository.
     */
    @Override
    public void close() {
        owner = null;
        super.close();
    }

    public static JavaBeanOperationRepository create(final String resourceName,
                                                     final ManagedResourceConnector connector,
                                                     final BeanInfo connectorInfo) {
        final ImmutableList<MethodDescriptor> properties = ImmutableList.copyOf(connectorInfo.getMethodDescriptors());

        return new JavaBeanOperationRepository(resourceName, connector) {

            @Override
            protected ImmutableList<MethodDescriptor> getMethods() {
                return properties;
            }
        };
    }
}
