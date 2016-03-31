package com.bytex.snamp.management.shell;

import com.bytex.snamp.management.SnampComponentDescriptor;
import org.osgi.framework.Bundle;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class Utils {
    private Utils(){

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

    static String getStateString(final SnampComponentDescriptor component){
        return getStateString(component.getState());
    }
}
