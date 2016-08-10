package com.bytex.snamp.gateway.ssh;

import com.bytex.snamp.internal.OperatingSystem;
import jline.console.ConsoleReader;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class TtyOutputStream extends FilterOutputStream {
    TtyOutputStream(final OutputStream underlyingStream) {
        super(underlyingStream);
    }

    @Override
    public void write(final int i) throws IOException {
        super.write(i);
        // workaround for MacOSX and Linux reset line after CR..
        if (i == ConsoleReader.CR.charAt(0))
            super.write(ConsoleReader.RESET_LINE);
    }

    static boolean needToApply() {
        return OperatingSystem.isLinux() || OperatingSystem.isMacOSX();
    }
}
