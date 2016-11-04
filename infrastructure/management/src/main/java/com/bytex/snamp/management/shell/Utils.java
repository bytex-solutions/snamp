package com.bytex.snamp.management.shell;

import com.bytex.snamp.core.SnampComponentDescriptor;
import org.osgi.framework.Bundle;

/**
 * @author Roman Sakno
 * @version 2.0
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

    static void appendln(final StringBuilder builder,
                                         final String value){
         newLine(builder.append(value));
    }

    static void appendln(final StringBuilder builder,
                                         final String format,
                                         final Object... args){
        append(builder, format, args);
        newLine(builder);
    }

    static void append(final StringBuilder builder,
                                       final String format,
                                       final Object... args){
        builder.append(String.format(format, args));
    }

    static void newLine(final StringBuilder output) {
        output.append(System.lineSeparator());
    }
}
