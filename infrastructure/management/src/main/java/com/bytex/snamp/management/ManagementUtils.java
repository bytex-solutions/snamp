package com.bytex.snamp.management;

import com.bytex.snamp.core.SnampComponentDescriptor;
import org.osgi.framework.Bundle;

/**
 * The type ManagementUtils.
 *
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class ManagementUtils {
    private ManagementUtils(){
        throw new InstantiationError();
    }

    private static String getStateString(final int state){
        switch (state){
            case Bundle.ACTIVE: return "ACTIVE";
            case Bundle.INSTALLED: return "INSTALLED";
            case Bundle.RESOLVED: return "RESOLVED";
            case Bundle.STARTING: return "STARTING";
            case Bundle.STOPPING: return "STOPPING";
            case Bundle.UNINSTALLED: return "UNINSTALLED";
            default: return Integer.toHexString(state);
        }
    }

    /**
     * Get state string string.
     *
     * @param component the component
     * @return the string
     */
    public static String getStateString(final SnampComponentDescriptor component){
        return getStateString(component.getState());
    }
}
