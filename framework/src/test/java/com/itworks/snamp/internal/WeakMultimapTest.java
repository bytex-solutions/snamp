package com.itworks.snamp.internal;

import com.google.common.collect.HashMultimap;
import com.itworks.snamp.internal.WeakMultimap;
import org.junit.Assert;
import org.junit.Test;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class WeakMultimapTest extends Assert {


    @Test
    public void removeUnusedTest(){
        final ReferenceQueue<Object> queue = new ReferenceQueue<>();
        final HashMultimap<String, WeakReference<Object>> map = HashMultimap.create(2, 3);
        map.put("a", new WeakReference<>(new Object(), queue));
        map.put("a", new WeakReference<>(new Object(), queue));
        map.put("b", new WeakReference<>(new Object(), queue));
        System.gc();
        while (true){
            final Reference<?> ref = queue.poll();
            if(ref == null) break;
        }
        WeakMultimap.removeUnused(map);
        assertEquals(0, map.size());
    }
}
