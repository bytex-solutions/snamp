package com.bytex.snamp.instrumentation;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Provides different internal utilities.
 */
final class Utils {
    private static final boolean IS_IN_OSGI;

    static {
        Class<?> bundleClass;
        try {
            bundleClass = Class.forName("org.osgi.framework.Bundle");
        } catch (final ClassNotFoundException e) {
            bundleClass = null;
        }
        IS_IN_OSGI = bundleClass != null;
    }

    private static String getFrameworkPropertyImpl(final Class<?> caller, final String propertyName){
        final Bundle bundle =  FrameworkUtil.getBundle(caller);
        return bundle == null ? null : bundle.getBundleContext().getProperty(propertyName);
    }

    static String getFrameworkProperty(final Class<?> caller, final String propertyName){
        return IS_IN_OSGI ? getFrameworkPropertyImpl(caller, propertyName) : null;
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
