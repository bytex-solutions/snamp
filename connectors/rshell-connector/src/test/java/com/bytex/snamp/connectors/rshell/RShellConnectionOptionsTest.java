package com.bytex.snamp.connectors.rshell;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class RShellConnectionOptionsTest extends Assert {
    @Test
    public void localProcessTest() throws Exception {
        final RShellConnectionOptions options =
                new RShellConnectionOptions("process", Collections.emptyMap());
        assertNotNull(options.createExecutionChannel());
    }
}
