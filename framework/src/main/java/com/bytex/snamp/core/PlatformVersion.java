package com.bytex.snamp.core;

import com.bytex.snamp.io.IOUtils;
import org.osgi.framework.Version;

import java.io.IOException;
import java.io.InputStream;

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
        try(final InputStream versionStream = PlatformVersion.class.getClassLoader().getResourceAsStream("PlatformVersion")) {
            String version = IOUtils.toString(versionStream);
            /*
                Strange workaround, yeah. Without this line of code the version will not be parsed
                from Karaf console
             */
            version = version.replace("\n", "");
            return new PlatformVersion(version);
        } catch (final IOException ignored) {
            return EMPTY;
        }
    }
}
