package com.bytex.snamp.internal;

import com.google.common.base.StandardSystemProperty;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class OperatingSystem {

    private OperatingSystem(){

    }

    private static String getOS(){
        return StandardSystemProperty.OS_NAME.value();
    }

    /**
     * Determines whether the underlying OS is Linux.
     * @return {@literal true}, if underlying OS is Linux.
     */
    public static boolean isLinux(){
        return getOS().startsWith("LINUX") || getOS().startsWith("Linux");
    }

    /**
     * Determines whether the underlying OS is MS Windows.
     * @return {@literal true}, if underlying OS is MS Windows.
     */
    public static boolean isWindows(){
        return getOS().startsWith("Windows");
    }

    /**
     * Determines whether the underlying OS is MacOS X.
     * @return {@literal true}, if underlying OS is MacOS X.
     */
    public static boolean isMacOSX(){
        return getOS().startsWith("Mac OS X");
    }
}
