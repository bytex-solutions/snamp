package com.bytex.snamp.internal;

import com.bytex.snamp.Box;
import com.bytex.snamp.SpecialUse;
import org.junit.Assert;
import org.junit.Test;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.BigInteger;
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
        final Box<String> box = new Box<>("");
        final Consumer<String> setter = reflectSetter(MethodHandles.lookup(), box, box.getClass().getDeclaredMethod("set", Object.class));
        setter.accept("Frank Underwood");
        assertEquals("Frank Underwood", box.get());
    }
}
