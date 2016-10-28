package com.bytex.snamp.core;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.io.IOUtils;
import org.osgi.framework.Version;

import java.io.InputStream;
import java.util.function.Supplier;

/**
 * Represents version of SNAMP platform.
 * This class cannot be inherited or instantiated directly from your code.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class PlatformVersion extends Version {
    private static final LazySoftReference<PlatformVersion> CURRENT_VERSION = new LazySoftReference<>();

    private static final Supplier<PlatformVersion> INITIALIZER = Utils.suspendException(() -> {
        try (final InputStream versionStream = PlatformVersion.class.getClassLoader().getResourceAsStream("PlatformVersion")) {
            String version = IOUtils.toString(versionStream);
            /*
                Strange workaround, yeah. Without this line of code the version will not be parsed
                from Karaf console
             */
            version = version.replace("\n", "");
            return new PlatformVersion(version);
        }
    });

    private PlatformVersion(final String version){
        super(version);
    }

    /**
     * Gets version of SNAMP platform.
     * @return Version of SNAMP platform.
     */
    public static PlatformVersion get() {
        return CURRENT_VERSION.lazyGet(INITIALIZER);
    }
}
