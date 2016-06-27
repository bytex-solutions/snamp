package com.bytex.snamp;

import com.bytex.snamp.concurrent.LazyValue;
import com.bytex.snamp.concurrent.LazyContainers;
import com.google.common.base.MoreObjects;
import com.bytex.snamp.internal.Utils;

import java.io.Closeable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Represents Java resource reader.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class ResourceReader implements Closeable, SafeCloseable {
    /**
     * The name of the resource.
     */
    public final String resourceName;
    private final LazyValue<ResourceBundle> bundle;

    public ResourceReader(final String baseName){
        resourceName = Utils.getFullyQualifiedResourceName(getClass(), baseName);
        bundle = LazyContainers.SOFT_REFERENCED.create(() -> getBundleImpl(Locale.getDefault()));
    }

    private ResourceBundle getBundleImpl(final Locale loc) {
        return ResourceBundle.getBundle(resourceName,
                    MoreObjects.firstNonNull(loc, Locale.getDefault()),
                    getClass().getClassLoader());
    }

    /**
     * Gets resource bundle associated with this reader.
     * @param loc The locale of the returned resource.
     * @return The resource bundle.
     * @throws MissingResourceException No resource bundle can be found
     */
    public final ResourceBundle getBundle(final Locale loc) throws MissingResourceException {
        return bundle.get(() -> getBundleImpl(loc));
    }

    /**
     * Loads string from the resource.
     * @param key The name of the string.
     * @param loc The requested localization of the resource. May be {@literal null}.
     * @param defval The default value of the resource string if it is not available.
     * @return The string loaded from the resource.
     */
    public final String getString(final String key, final Locale loc, final String defval) {
        final ResourceBundle bnd = getBundle(loc);
        try {
            return bnd != null && bnd.containsKey(key) ? bnd.getString(key) : defval;
        } catch (final MissingResourceException e) {
            return defval;
        }
    }

    public final boolean getBoolean(final String key, final Locale loc, final boolean defval) {
        return Boolean.valueOf(getString(key, loc, Boolean.toString(defval)));
    }

    public final byte getByte(final String key, final Locale loc, final byte defval){
        return Byte.parseByte(getString(key, loc, Byte.toString(defval)));
    }

    public final int getInt(final String key, final Locale loc, final int defval){
        return Integer.parseInt(getString(key, loc, Integer.toString(defval)));
    }

    public final long getLong(final String key, final Locale loc, final long defval){
        return Long.parseLong(getString(key, loc, Long.toString(defval)));
    }

    public final float getFloat(final String key, final Locale loc, final float defval){
        return Float.parseFloat(getString(key, loc, Float.toString(defval)));
    }

    public final double getDouble(final String key, final Locale loc, final double defval){
        return Double.parseDouble(getString(key, loc, Double.toString(defval)));
    }

    public final char getChar(final String key, final Locale loc, final char defval) {
        final String str = getString(key, loc, new String(new char[]{defval}));
        return str.isEmpty() ? defval : str.charAt(0);
    }

    /**
     * Releases resource cache associated with this reader.
     */
    @Override
    public void close() {
        bundle.reset();
        ResourceBundle.clearCache(getClass().getClassLoader());
    }
}
