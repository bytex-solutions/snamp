package com.bytex.snamp.adapters.syslog;

import java.lang.management.ManagementFactory;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class SysLogUtils {
    private SysLogUtils(){

    }

    static String getProcessId(final String fallback) {
        // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');
        // part before '@' empty (index = 0) / '@' not found (index = -1)
        return index < 1 ? fallback : jvmName.substring(0, index);
    }
}
