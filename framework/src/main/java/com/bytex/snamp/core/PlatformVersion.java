package com.bytex.snamp.core;

import com.bytex.snamp.concurrent.LazyValueFactory;
import com.bytex.snamp.concurrent.LazyValue;
import com.bytex.snamp.io.IOUtils;
import org.osgi.framework.Version;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents version of SNAMP platform.
 * This class cannot be inherited or instantiated directly from your code.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class PlatformVersion extends Version {
    private static final LazyValue<PlatformVersion> CURRENT_VERSION = LazyValueFactory.THREAD_SAFE_SOFT_REFERENCED.of(PlatformVersion::getCurrentVersionImpl);

    private PlatformVersion(final String version){
        super(version);
    }

    private PlatformVersion(){
        super(0, 0, 0);
    }

    private static PlatformVersion getCurrentVersionImpl() {
        try (final InputStream versionStream = PlatformVersion.class.getClassLoader().getResourceAsStream("PlatformVersion")) {
            String version = IOUtils.toString(versionStream);
            /*
                Strange workaround, yeah. Without this line of code the version will not be parsed
                from Karaf console
             */
            version = version.replace("\n", "");
            return new PlatformVersion(version);
        } catch (final IOException ignored) {
            return new PlatformVersion();
        }
    }

    /**
     * Gets version of SNAMP platform.
     * @return Version of SNAMP platform.
     */
    public static PlatformVersion get() {
        return CURRENT_VERSION.get();
    }
}
