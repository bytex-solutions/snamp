package com.bytex.snamp.core;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.io.IOUtils;
import org.osgi.framework.Version;

import java.io.IOException;
import java.io.InputStream;

import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * Represents version of SNAMP platform.
 * This class cannot be inherited or instantiated directly from your code.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class PlatformVersion extends Version {
    private static final LazySoftReference<PlatformVersion> CURRENT_VERSION = new LazySoftReference<>();

    private PlatformVersion(final String version){
        super(version);
    }

    private static PlatformVersion getChecked() throws IOException{
        try (final InputStream versionStream = PlatformVersion.class.getClassLoader().getResourceAsStream("PlatformVersion")) {
            String version = IOUtils.toString(versionStream);
            /*
                Strange workaround. Without this line of code the version will not be parsed
                from Karaf console
             */
            version = version.replace("\n", "");
            return new PlatformVersion(version);
        }
    }

    /**
     * Gets version of SNAMP platform.
     * @return Version of SNAMP platform.
     */
    public static PlatformVersion get() {
        return CURRENT_VERSION.lazyGet(() -> callUnchecked(PlatformVersion::getChecked));
    }
}
