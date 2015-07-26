package com.bytex.snamp.jmx;

import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenType;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Comparator;

/**
 * Simplifies instantiation of {@link javax.management.openmbean.OpenMBeanAttributeInfoSupport}
 * instances.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class OpenMBeanAttributeInfoFactory {
    private static final MethodHandle OpenMBeanAttributeInfoSupportConstructor;

    static {
        final MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        try {
            final Constructor<OpenMBeanAttributeInfoSupport> ctor =
                    OpenMBeanAttributeInfoSupport.class.getConstructor(String.class,
                            String.class,
                            OpenType.class,
                            boolean.class,
                            boolean.class,
                            boolean.class,
                            Object.class,
                            Comparable.class,
                            Comparator.class);
            OpenMBeanAttributeInfoSupportConstructor =
                    lookup.unreflectConstructor(ctor);
        }catch (final ReflectiveOperationException e){
            throw new ExceptionInInitializerError(e);
        }
    }

    private OpenMBeanAttributeInfoFactory(){

    }

    public static OpenMBeanAttributeInfoSupport create(final String     name,
                                                       final String     description,
                                                       final OpenType<?> openType,
                                                       final boolean    isReadable,
                                                       final boolean    isWritable,
                                                       final boolean    isIs,
                                                       final Object          defaultValue,
                                                       final Comparable<?> minValue,
                                                       final Comparable<?> maxValue) throws Exception {
        try {
            return (OpenMBeanAttributeInfoSupport) OpenMBeanAttributeInfoSupportConstructor.invokeExact(name,
                    description,
                    openType,
                    isReadable,
                    isWritable,
                    isIs,
                    defaultValue,
                    minValue,
                    maxValue);
        } catch (final Exception | Error e) {
            throw e;
        } catch (final Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }
}
