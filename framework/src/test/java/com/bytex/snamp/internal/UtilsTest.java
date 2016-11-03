package com.bytex.snamp.internal;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.Box;
import com.bytex.snamp.BoxFactory;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.SpinWait;
import org.junit.Assert;
import org.junit.Test;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Executor;
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

    @SpecialUse
    private static BigInteger getBigInteger(){
        return BigInteger.TEN;
    }

    @Test
    public void reflectGetterTest() throws ReflectiveOperationException{
        Supplier<?> sup = reflectGetter(MethodHandles.lookup(), null, getClass().getDeclaredMethod("getBigInteger"));
        assertEquals(getBigInteger(), sup.get());
        final Object obj = new Object(){
            @SpecialUse
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
        final Box<String> box = BoxFactory.create("");
        final Consumer<String> setter = reflectSetter(MethodHandles.lookup(), box, box.getClass().getDeclaredMethod("set", Object.class));
        setter.accept("Frank Underwood");
        assertEquals("Frank Underwood", box.get());
    }

    @Test(expected = MalformedURLException.class)
    public void suspendExceptionsTest(){
        final Supplier<? extends String> sup = Utils.suspendException(() -> {
            throw new MalformedURLException();
        });
        sup.get();
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
        final Executor executor = Executors.newFixedThreadPool(3);
        final AtomicInteger index = new AtomicInteger(0);
        Utils.parallelForEach(Arrays.spliterator(ArrayUtils.wrapArray(bytes)), b -> index.incrementAndGet(), executor);
        SpinWait.spinUntil(() -> index.get() < 99, Duration.ofSeconds(2));
        assertEquals(99, index.get());
    }
}
