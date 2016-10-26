package com.bytex.snamp;

import com.google.common.base.MoreObjects;

import java.io.Closeable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Represents Java resource reader.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ResourceReader implements Closeable, SafeCloseable {
    /**
     * The name of the resource.
     */
    public final String resourceName;

    public ResourceReader(final String baseName){
        resourceName = getFullyQualifiedResourceName(getClass(), baseName);
    }

    protected ResourceReader(final Class<?> resourceLocator, final String baseName){
        resourceName = getFullyQualifiedResourceName(resourceLocator, baseName);
    }

    private static String getFullyQualifiedResourceName(final Class<?> locator, String name){
        if(locator.isArray())
            return getFullyQualifiedResourceName(locator.getComponentType(), name);
        else if (!name.startsWith("/")) {
            final String baseName = locator.getName();
            final int index = baseName.lastIndexOf('.');
            if (index != -1)
                name = String.format("%s/%s", baseName.substring(0, index).replace('.', '/'), name);
        }
        else name = name.substring(1);
        return name;
    }


    /**
     * Gets resource bundle associated with this reader.
     * @param loc The locale of the returned resource.
     * @return The resource bundle.
     * @throws MissingResourceException No resource bundle can be found
     */
    public final ResourceBundle getBundle(final Locale loc) throws MissingResourceException {
        return ResourceBundle.getBundle(resourceName,
                MoreObjects.firstNonNull(loc, Locale.getDefault()),
                getClass().getClassLoader());
    }

    /**
     * Loads string from the resource.
     * @param key The name of the string.
     * @param loc The requested localization of the resource. May be {@literal null}.
     * @param defval The default value of the resource string if it is not available.
     * @return The string loaded from the resource.
     */
    protected final String getString(final String key, final Locale loc, final String defval) {
        final ResourceBundle bnd = getBundle(loc);
        try {
            return bnd != null && bnd.containsKey(key) ? bnd.getString(key) : defval;
        } catch (final MissingResourceException e) {
            return defval;
        }
    }

    protected final boolean getBoolean(final String key, final Locale loc, final boolean defval) {
        return Boolean.valueOf(getString(key, loc, Boolean.toString(defval)));
    }

    /**
     * Releases resource cache associated with this reader.
     */
    @Override
    public void close() {
        ResourceBundle.clearCache(getClass().getClassLoader());
    }
}
