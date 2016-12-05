package com.bytex.snamp.instrumentation;

/**
 * Provides different internal utilities.
 */
final class Utils {
    static final boolean IS_IN_OSGI;

    static {
        Class<?> bundleClass;
        try {
            bundleClass = Class.forName("org.osgi.framework.Bundle");
        } catch (final ClassNotFoundException e) {
            bundleClass = null;
        }
        IS_IN_OSGI = bundleClass != null;
    }

    static String getSystemProperty(final String... propertyNames){
        for(final String name: propertyNames){
            final String value = System.getProperty(name);
            if(value != null && !value.isEmpty())
                return value;
        }
        return null;
    }
}
