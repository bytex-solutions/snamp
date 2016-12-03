package com.bytex.snamp.instrumentation.measurements;

/**
 * Provides different internal utilities.
 */
final class Utils {
    static String getSystemProperty(final String... propertyNames){
        for(final String name: propertyNames){
            final String value = System.getProperty(name);
            if(value != null && !value.isEmpty())
                return value;
        }
        return null;
    }
}
