package com.bytex.snamp.internal;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.bytex.snamp.internal.Utils.reflectGetter;
import static com.bytex.snamp.internal.Utils.reflectSetter;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class UtilsTest extends Assert {
    final class TestJavaBean{
        private BigInteger iValue = BigInteger.ZERO;
        private float fValue = 0F;

        public BigInteger getBigInteger() {
            return iValue;
        }

        public void setBigInteger(final BigInteger value){
            iValue = value;
        }

        public float getFloatValue(){
            return fValue;
        }

        public void setFloatValue(final float value){
            fValue = value;
        }
    }

    @SpecialUse(SpecialUse.Case.REFLECTION)
    private BigInteger sum(final BigInteger v){
        return BigInteger.TEN.add(v);
    }

    @Test
    public void spreaderTest() throws Throwable {
        final MethodHandle handle = MethodHandles.lookup().unreflect(getClass().getDeclaredMethod("sum", BigInteger.class));
        assertNotNull(handle);
        final MethodHandle spreader = MethodHandles.spreadInvoker(handle.type(), 1);
        final Object result = spreader.invoke(handle, this, new Object[]{BigInteger.ONE});
        assertEquals(BigInteger.valueOf(11), result);
    }

    @Test
    public void reflectGetterTest() throws ReflectiveOperationException{
        final TestJavaBean bean = new TestJavaBean();
        bean.setBigInteger(BigInteger.TEN);
        Function sup = reflectGetter(MethodHandles.lookup(), TestJavaBean.class.getDeclaredMethod("getBigInteger"));
        assertEquals(bean.getBigInteger(), sup.apply(bean));
        bean.setFloatValue(10F);
        sup = reflectGetter(MethodHandles.lookup(), TestJavaBean.class.getDeclaredMethod("getFloatValue"));
        assertEquals(bean.getFloatValue(), sup.apply(bean));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void reflectSetterTest() throws ReflectiveOperationException{
        final TestJavaBean bean = new TestJavaBean();
        BiConsumer setter = reflectSetter(MethodHandles.lookup(), TestJavaBean.class.getDeclaredMethod("setBigInteger", BigInteger.class));
        setter.accept(bean, BigInteger.ONE);
        assertEquals(BigInteger.ONE, bean.getBigInteger());
        setter = reflectSetter(MethodHandles.lookup(), TestJavaBean.class.getDeclaredMethod("setFloatValue", float.class));
        setter.accept(bean, 42);
        assertEquals(42F, bean.getFloatValue(), 0.1F);
    }

    @Test(expected = MalformedURLException.class)
    public void suspendExceptionsTest(){
        Utils.callUnchecked(() -> {
            throw new MalformedURLException();
        });
    }

    @Test
    public void callUncheckedTest(){
        final String s = Utils.callUnchecked(() -> new StringBuilder().append("Hello").append(", ").append("world").toString());
        assertEquals("Hello, world", s);
    }

    @Test
    public void parallelForEachTest() throws InterruptedException, TimeoutException {
        final SecureRandom random = new SecureRandom(new byte[]{10, 90, 67, 33, 91, 29});
        final byte[] bytes = new byte[100];
        random.nextBytes(bytes);
        final ExecutorService executor = Executors.newFixedThreadPool(3);
        final AtomicInteger index = new AtomicInteger(0);
        Utils.parallelForEach(Arrays.spliterator(ArrayUtils.wrapArray(bytes)), b -> index.incrementAndGet(), executor);
        assertEquals(100, index.get());
    }

    @Test(expected = IOException.class)
    public void closeAllTest() throws Exception {
        Utils.closeAll(() -> {
        }, () -> {
            throw new IOException();
        }, () -> {
            throw new IllegalArgumentException();
        });
    }

    @Test
    public void superCloseTest() throws Exception{
        class MyClass implements AutoCloseable{
            boolean baseClosed;

            @Override
            public void close() throws Exception {
                baseClosed = true;
            }
        }


        final class DerivedClass extends MyClass{
            boolean derivedClosed;

            private void closeDerived(){
                derivedClosed = true;
            }

            @Override
            public void close() throws Exception {
                Utils.closeAll(super::close, this::closeDerived);
            }
        }

        final DerivedClass instance = new DerivedClass();
        instance.close();
        assertTrue(instance.baseClosed);
        assertTrue(instance.derivedClosed);
    }
}
