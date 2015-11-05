package com.bytex.snamp.core;

import com.bytex.snamp.io.IOUtils;
import org.osgi.framework.Version;

import java.io.IOException;

/**
 * Represents version of SNAMP platform.
 * This class cannot be inherited or instantiated directly from your code.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class PlatformVersion extends Version {
    private static final PlatformVersion EMPTY = new PlatformVersion();

    private PlatformVersion(final String version){
        super(version);
    }

    private PlatformVersion(){
        super(0, 0, 0);
    }

    /**
     * Gets version of SNAMP platform.
     * @return Version of SNAMP platform.
     */
    public static PlatformVersion get() {
        try {
            final String version =
                    IOUtils.toString(PlatformVersion.class.getClassLoader().getResourceAsStream("PlatformVersion"));
            return new PlatformVersion(version);
        } catch (final IOException ignored) {
            return EMPTY;
        }
    }
}
