package com.bytex.snamp;

import com.google.common.base.MoreObjects;

import java.io.Closeable;
import java.util.*;

/**
 * Represents Java resource reader.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public class ResourceReader implements Closeable, SafeCloseable {
    /**
     * The name of the resource.
     */
    public final String resourceName;
    private final Class<?> resourceLocator;

    protected ResourceReader(final String baseName){
        resourceName = getFullyQualifiedResourceName(getClass(), baseName);
        resourceLocator = null;
    }

    public ResourceReader(final Class<?> resourceLocator, final String baseName){
        this.resourceLocator = Objects.requireNonNull(resourceLocator);
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
                resourceLocator == null ? getClass().getClassLoader() : resourceLocator.getClassLoader());
    }

    /**
     * Gets string from source that differs from the resource bundle.
     * @param key The name of the string.
     * @param loc The requested localization of the resource. May be {@literal null}.
     * @return Optional string loaded from different resource.
     */
    protected Optional<String> getStringFallback(final String key, final Locale loc){
        return Optional.empty();
    }

    /**
     * Loads string from the resource.
     * @param key The name of the string.
     * @param loc The requested localization of the resource. May be {@literal null}.
     * @return The string loaded from the resource.
     */
    public final Optional<String> getString(final String key, final Locale loc) {
        final ResourceBundle bnd = getBundle(loc);
        if (bnd != null && bnd.containsKey(key))
            try {
                return Optional.of(bnd.getString(key));
            } catch (final MissingResourceException e) {
                return getStringFallback(key, loc);
            }
        else
            return getStringFallback(key, loc);
    }

    public final Optional<Boolean> getBoolean(final String key, final Locale loc) {
        return getString(key, loc).map(Boolean::valueOf);
    }

    /**
     * Releases resource cache associated with this reader.
     */
    @Override
    public void close() {
        ResourceBundle.clearCache(getClass().getClassLoader());
    }
}
