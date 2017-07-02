package com.bytex.snamp.internal;

import com.google.common.base.StandardSystemProperty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class OperatingSystem {

    private OperatingSystem(){
        throw new InstantiationError();
    }

    private static boolean systemNameStartsWith(final String... prefix){
        final String os = StandardSystemProperty.OS_NAME.value();
        if(os != null)
            for(final String p: prefix)
                if(os.startsWith(p))
                    return true;
        return false;
    }

    /**
     * Determines whether the underlying OS is Linux.
     * @return {@literal true}, if underlying OS is Linux.
     */
    public static boolean isLinux(){
        return systemNameStartsWith("LINUX", "Linux");
    }

    /**
     * Determines whether the underlying OS is MS Windows.
     * @return {@literal true}, if underlying OS is MS Windows.
     */
    public static boolean isWindows(){
        return systemNameStartsWith("Windows");
    }

    /**
     * Determines whether the underlying OS is MacOS X.
     * @return {@literal true}, if underlying OS is MacOS X.
     */
    public static boolean isMacOSX(){
        return systemNameStartsWith("Mac OS X");
    }
}
