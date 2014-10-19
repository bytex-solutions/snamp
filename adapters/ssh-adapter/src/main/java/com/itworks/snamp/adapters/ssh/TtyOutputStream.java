package com.itworks.snamp.adapters.ssh;

import jline.console.ConsoleReader;
import org.apache.commons.lang3.SystemUtils;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Roman Sakno
 * @version 1.0
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
        return SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX;
    }
}
