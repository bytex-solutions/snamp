package com.bytex.snamp.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class PlatformVersionTest extends Assert {
    @Test
    public void versionTest(){
        assertTrue(PlatformVersion.get().getMajor() > 0);
    }
}
