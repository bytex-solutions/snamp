package com.bytex.snamp.connectors.rshell;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class RShellConnectionOptionsTest extends Assert {
    @Test
    public void localProcessTest() throws Exception {
        final RShellConnectionOptions options =
                new RShellConnectionOptions("process", Collections.<String, String>emptyMap());
        assertNotNull(options.createExecutionChannel());
    }
}
