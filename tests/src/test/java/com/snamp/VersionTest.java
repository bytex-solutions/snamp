package com.snamp;

import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class VersionTest extends SnampClassTestSet<Version> {
    @Test
    public final void versionStringCreationTest(){
        assertEquals("1.2", Version.toString(1, 2));
        assertEquals("1.4.42", Version.toString(1, 4, 42));
        assertEquals("1.4.42.566", Version.toString(1, 4, 42, 566));
    }

    @Test
    public final void versionComparisonTest(){
        assertTrue(Version.compare("1.2", "1.*") == 0);
        assertTrue(Version.compare("1.2.1", "1.2") > 0);
        assertTrue(Version.compare("1.2.*", "1.2") > 0);
        assertTrue(Version.compare("1.2", "1.2.1") < 0);
    }
}
