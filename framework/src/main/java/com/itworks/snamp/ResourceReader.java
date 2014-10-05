package com.itworks.snamp;

import com.itworks.snamp.internal.Utils;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Represents Java resourc reader.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class ResourceReader {

    /**
     * Retrieves resource accessor for the specified locale.
     * <p>
     *     The following example shows recommended implementation of this method:
     *     <pre><code>
     *     protected final ResourceBundle getBundle(final Locale loc) {
     *      return loc != null ? ResourceBundle.getBundle(getResourceName("MyResource"), loc) :
     *      ResourceBundle.getBundle(getResourceName("MyResource"));
     *     }
     *     </code></pre>
     * </p>
     * @param loc The requested localization of the resource. May be {@literal null}.
     * @return The resource accessor.
     */
    protected abstract ResourceBundle getBundle(final Locale loc);

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

    /**
     * Returns full resource name constructed from the namespace of the derived class and the
     * specified resource name.
     * @param baseName The name of the resource impl.
     * @return The full resource name.
     */
    protected final String getResourceName(final String baseName){
        return Utils.getFullyQualifiedResourceName(getClass(), baseName);
    }
}
