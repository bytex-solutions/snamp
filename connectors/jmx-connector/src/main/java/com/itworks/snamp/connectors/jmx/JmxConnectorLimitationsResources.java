package com.itworks.snamp.connectors.jmx;


import com.itworks.snamp.ResourceReader;

import java.lang.ref.SoftReference;
import java.util.Locale;
import java.util.ResourceBundle;

final class JmxConnectorLimitationsResources extends ResourceReader {
    private static final String RESOURCE_NAME = "JmxConnectorLimitations";
    private static SoftReference<JmxConnectorLimitationsResources> instance;

    private JmxConnectorLimitationsResources() {

    }

    public static synchronized JmxConnectorLimitationsResources getInstance() {
        JmxConnectorLimitationsResources result;
        if (instance == null || (result = instance.get()) == null)
            instance = new SoftReference<>(result = new JmxConnectorLimitationsResources());
        return result;
    }

    /**
     * Retrieves resource accessor for the specified locale.
     * <p>
     * The following example shows recommended implementation of this method:
     * <pre><code>
     *     protected final ResourceBundle getBundle(final Locale loc) {
     *      return loc != null ? ResourceBundle.getBundle(getResourceName("MyResource"), loc) :
     *      ResourceBundle.getBundle(getResourceName("MyResource"));
     *     }
     *     </code></pre>
     * </p>
     *
     * @param loc The requested localization of the resource. May be {@literal null}.
     * @return The resource accessor.
     */
    @Override
    protected ResourceBundle getBundle(final Locale loc) {
        return loc != null ? ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc) :
                ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
    }

    String getMaxInstanceCountDescription(final long maxInstanceCount, final Locale loc) {
        return String.format(getString("maxInstanceCount", loc, "%s"), maxInstanceCount);
    }

    String getMaxAttributeCountDescription(final long maxAttributeCount, final Locale loc) {
        return String.format(getString("maxAttributeCount", loc, "%s"), maxAttributeCount);
    }

    String getMaxVersionDescription(final String ver, final Locale loc) {
        return String.format(getString("maxVersion", loc, "%s"), ver);
    }
}
