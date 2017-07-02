package com.bytex.snamp.internal;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.Box;
import com.bytex.snamp.SpecialUse;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.bytex.snamp.internal.Utils.reflectGetter;
import static com.bytex.snamp.internal.Utils.reflectSetter;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class UtilsTest extends Assert {

    @SpecialUse(SpecialUse.Case.REFLECTION)
    private static BigInteger getBigInteger(){
        return BigInteger.TEN;
    }

    @SpecialUse(SpecialUse.Case.REFLECTION)
    private BigInteger sum(final BigInteger v){
        return getBigInteger().add(v);
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
        Supplier<?> sup = reflectGetter(MethodHandles.lookup(), null, getClass().getDeclaredMethod("getBigInteger"));
        assertEquals(getBigInteger(), sup.get());
        final Object obj = new Object(){
            @SpecialUse(SpecialUse.Case.REFLECTION)
            public BigDecimal getBigDecimal(){
                return BigDecimal.ONE;
            }
        };
        sup = reflectGetter(MethodHandles.lookup(), obj, obj.getClass().getDeclaredMethod("getBigDecimal"));
        assertEquals(BigDecimal.ONE, sup.get());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void reflectSetterTest() throws ReflectiveOperationException{
        final Box<String> box = Box.of("");
        final Consumer<String> setter = reflectSetter(MethodHandles.lookup(), box, Box.class.getDeclaredMethod("set", Object.class));
        setter.accept("Frank Underwood");
        assertEquals("Frank Underwood", box.get());
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
