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

    /**
     * Appendln.
     *
     * @param builder the builder
     * @param value   the value
     */
    public static void appendln(final StringBuilder builder,
                                         final String value){
         newLine(builder.append(value));
    }

    /**
     * Appendln.
     *
     * @param builder the builder
     * @param format  the format
     * @param args    the args
     */
    public static void appendln(final StringBuilder builder,
                                         final String format,
                                         final Object... args){
        append(builder, format, args);
        newLine(builder);
    }

    /**
     * Append.
     *
     * @param builder the builder
     * @param format  the format
     * @param args    the args
     */
    public static void append(final StringBuilder builder,
                                       final String format,
                                       final Object... args){
        builder.append(String.format(format, args));
    }

    /**
     * New line.
     *
     * @param output the output
     */
    public static void newLine(final StringBuilder output) {
        output.append(System.lineSeparator());
    }
}
