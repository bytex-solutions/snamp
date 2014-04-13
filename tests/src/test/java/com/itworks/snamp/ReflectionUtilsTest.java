package com.itworks.snamp;

import com.itworks.snamp.internal.ReflectionUtils;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ReflectionUtilsTest extends SnampClassTestSet<ReflectionUtils> {
    public static interface SampleInterface{
        int sum(int a, int b);
    }

    private static final class SampleInterfaceImpl implements SampleInterface{

        @Override
        public int sum(final int a, final int b) {
            return a + b;
        }
    }

    @Test
    public final void isolateTest(){
        final SampleInterfaceImpl impl = new SampleInterfaceImpl();
        final SampleInterface iface = ReflectionUtils.isolate(impl, SampleInterface.class);
        assertFalse(iface instanceof SampleInterfaceImpl);
        assertEquals(42, iface.sum(3, 39));
    }
}