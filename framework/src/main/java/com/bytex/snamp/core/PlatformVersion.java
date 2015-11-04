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
    public static final PlatformVersion INSTANCE;

    static {
        final String version;
        try{
             version =
                    IOUtils.toString(PlatformVersion.class.getClassLoader().getResourceAsStream("PlatformVersion"));
        }catch (final IOException e){
            throw new ExceptionInInitializerError(e);
        }
        INSTANCE = new PlatformVersion(version);
    }

    private PlatformVersion(final String version){
        super(version);
    }
}
