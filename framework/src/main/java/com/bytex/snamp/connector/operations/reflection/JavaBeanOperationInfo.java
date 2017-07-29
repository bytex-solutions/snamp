package com.bytex.snamp.connector.operations.reflection;

import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.jmx.WellKnownType;

import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.OpenType;
import java.beans.MethodDescriptor;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import static com.bytex.snamp.internal.Utils.callAndWrapException;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents operation reflected from JavaBean method.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public class JavaBeanOperationInfo extends OpenMBeanOperationInfoSupport {
    private static final long serialVersionUID = 5144309275413329193L;
    private final MethodHandle handle;
    private final MethodHandle spreadHandle;

    public JavaBeanOperationInfo(final String operationName,
                                 final MethodDescriptor method,
                                 final OperationDescriptor descriptor) throws ReflectionException {
        super(operationName,
                getDescription(method),
                getParameters(method),
                getReturnType(method),
                getImpact(method),
                descriptor);
        handle = callAndWrapException(() -> MethodHandles.publicLookup().unreflect(method.getMethod()), ReflectionException::new);
        spreadHandle = MethodHandles.spreadInvoker(handle.type(), 1);
    }

    private static String getDescription(final MethodDescriptor method) {
        final ManagementOperation operationInfo = method.getMethod().getAnnotation(ManagementOperation.class);
        return operationInfo != null ? operationInfo.description() : method.getShortDescription();
    }

    private static int getImpact(final MethodDescriptor method) {
        final ManagementOperation operationInfo = method.getMethod().getAnnotation(ManagementOperation.class);
        return operationInfo != null ? operationInfo.impact() : MBeanOperationInfo.UNKNOWN;
    }

    private static OpenType<?> getReturnType(final MethodDescriptor method) throws ReflectionException {
        return getType(method.getMethod().getReturnType());
    }

    private static <A extends Annotation> A getParameterAnnotation(final Method method,
                                                                   final int parameterIndex,
                                                                   final Class<A> annotationType) {
        final Annotation[][] annotations = method.getParameterAnnotations();
        if (annotations.length >= parameterIndex)
            return null;
        for (final Annotation candidate : annotations[parameterIndex])
            if (annotationType.isInstance(candidate))
                return annotationType.cast(candidate);
        return null;
    }

    private static OpenMBeanParameterInfoSupport[] getParameters(final MethodDescriptor method) throws ReflectionException {
        final Class<?>[] parameters = method.getMethod().getParameterTypes();
        final OpenMBeanParameterInfoSupport[] result = new OpenMBeanParameterInfoSupport[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            final OperationParameter metadata = getParameterAnnotation(method.getMethod(), i, OperationParameter.class);
            final String name;
            final String description;
            if (metadata == null)
                description = name = Integer.toString(i);
            else {
                name = metadata.name();
                description = isNullOrEmpty(metadata.description()) ?
                        Integer.toString(i) :
                        metadata.description();
            }
            result[i] = new OpenMBeanParameterInfoSupport(name,
                    description,
                    getType(parameters[i]));
        }
        return result;
    }

    private static OpenType<?> getType(final Class<?> type) throws ReflectionException {
        final WellKnownType knownType = WellKnownType.getType(type);
        if (knownType == null || !knownType.isOpenType())
            throw new ReflectionException(new OpenDataException(String.format("Invalid parameter type '%s'", type)));
        else
            return knownType.getOpenType();
    }

    /**
     * Invokes JavaBean method using fast reflection.
     * @param owner {@code this} reference. Cannot be {@literal null}.
     * @param args An array of method arguments
     * @return Invocation result.
     * @throws ReflectionException Unable to invoke method.
     */
    public final Object invoke(final Object owner, final Object... args) throws ReflectionException{
        try {
            return spreadHandle.invoke(handle, owner, args);
        } catch (final Exception e) {
            throw new ReflectionException(e);
        } catch (final Error e){
            throw e;
        } catch (final Throwable e){
            throw new InternalError(e);
        }
    }

    public static boolean isValidDescriptor(final MethodDescriptor descriptor){
        return descriptor.getMethod().isAnnotationPresent(ManagementOperation.class);
    }
}
