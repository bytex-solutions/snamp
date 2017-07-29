package com.bytex.snamp.connector.operations.reflection;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.time.Duration;
import java.util.Collection;

/**
 * Represents tests for {@link JavaBeanOperationRepository} and {@link JavaBeanOperationInfo}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class JavaBeanOperationTest extends Assert {
    @ManagementOperation(description = "Computes sum of two values")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    public int sum(final int x, final int y){
        return x + y;
    }

    @Test
    public void operationInfoTest() throws ReflectiveOperationException, ReflectionException {
        final MethodDescriptor descriptor = new MethodDescriptor(getClass().getMethod("sum", int.class, int.class));
        final JavaBeanOperationInfo operationInfo = new JavaBeanOperationInfo("sum", descriptor, new OperationDescriptor(Duration.ofSeconds(1), ImmutableMap.of()));
        final int result = (int) operationInfo.invoke(this, 5, 6);
        assertEquals(5 + 6, result);
    }

    private static JavaBeanOperationRepository createTestRepository(final JavaBeanOperationTest owner) throws IntrospectionException {
        final ImmutableList<MethodDescriptor> info = ImmutableList.copyOf(Introspector.getBeanInfo(owner.getClass(), Assert.class).getMethodDescriptors());
        return new JavaBeanOperationRepository("test", owner) {
            @Override
            protected Collection<MethodDescriptor> getMethods() {
                return info;
            }
        };
    }

    @Test
    public void repositoryTest() throws IntrospectionException, MBeanException, ReflectionException {
        try(final JavaBeanOperationRepository repository = createTestRepository(this)) {
            assertNotNull(repository.enableOperation("sum", new OperationDescriptor(Duration.ofSeconds(1), ImmutableMap.of())));
            final int result = (int) repository.invoke("sum", new Object[]{10, 20}, ArrayUtils.emptyArray(String[].class));
            assertEquals(10 + 20, result);
        }
    }
}
