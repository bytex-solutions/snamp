package com.bytex.snamp.core;

import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.io.IOUtils;
import org.osgi.framework.Version;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Represents version of SNAMP platform.
 * This class cannot be inherited or instantiated directly from your code.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class PlatformVersion extends Version {
    private static final LazyReference<PlatformVersion> CURRENT_VERSION = LazyReference.soft();

    private PlatformVersion(final String version){
        super(version);
    }

    private static PlatformVersion getImpl(){
        try (final InputStream versionStream = PlatformVersion.class.getClassLoader().getResourceAsStream("PlatformVersion")) {
            String version = IOUtils.toString(versionStream);
            /*
                Strange workaround. Without this line of code the version will not be parsed
                from Karaf console
             */
            version = version.replace("\n", "");
            return new PlatformVersion(version);
        } catch (final IOException e){
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Gets version of SNAMP platform.
     * @return Version of SNAMP platform.
     */
    public static PlatformVersion get() {
        return CURRENT_VERSION.get(PlatformVersion::getImpl);
    }
}
