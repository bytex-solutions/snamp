package com.itworks.snamp.adapters.jmx;

import com.google.common.base.Supplier;
import com.itworks.snamp.core.AbstractBundleActivator;
import com.itworks.snamp.licensing.AbstractLicenseLimitations;
import com.itworks.snamp.licensing.FrameworkServiceLimitations;
import com.itworks.snamp.licensing.LicenseReader;
import com.itworks.snamp.licensing.LicensingException;
import org.osgi.framework.Version;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * JMX adapter license limitations.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@XmlRootElement(name = "jmxAdapterLimitations")
public final class JmxAdapterLicenseLimitations extends AbstractLicenseLimitations implements FrameworkServiceLimitations<JmxResourceAdapter> {
    public static final AbstractBundleActivator.RequiredServiceAccessor<LicenseReader> licenseReader = new AbstractBundleActivator.SimpleDependency<>(LicenseReader.class);

    static final Supplier<JmxAdapterLicenseLimitations> fallbackFactory = new Supplier<JmxAdapterLicenseLimitations>() {
        @Override
        public JmxAdapterLicenseLimitations get() {
            return new JmxAdapterLicenseLimitations();
        }
    };

    public JmxAdapterLicenseLimitations(){
        this("0.0", false);
    }

    private JmxAdapterLicenseLimitations(final String version, final boolean notificationsEnabled){
        maxVersion = PluginVersionLimitationAdapter.createLimitation(version);
        this.notificationsEnabled = NotificationFeatureLimitationAdapter.createLimitation(notificationsEnabled);
    }

    /**
     * Returns the currently loaded limitations.
     * @return The currently loaded limitations.
     */
    public static JmxAdapterLicenseLimitations current(){
        return current(JmxAdapterLicenseLimitations.class, licenseReader, fallbackFactory);
    }

    private static final class NotificationFeatureLimitationAdapter extends RequirementParser<Boolean, Boolean, ExactLimitation<Boolean>>{

        public static ExactLimitation<Boolean> createLimitation(final boolean notificationsEnabled){
            return new ExactLimitation<Boolean>(notificationsEnabled) {
                @Override
                public LicensingException createException() {
                    return new LicensingException("JMX notifications are not allowed.");
                }
            };
        }

        /**
         * Convert a value type to a bound type.
         *
         * @param v The value to be converted. Can be null.
         * @throws Exception if there's an error during the conversion. The caller is responsible for
         *                   reporting the error to the user through {@link javax.xml.bind.ValidationEventHandler}.
         */
        @Override
        public final ExactLimitation<Boolean> unmarshal(final Boolean v) throws Exception {
            return createLimitation(v != null && v);
        }
    }

    private static final class PluginVersionLimitationAdapter extends RequirementParser<Version, String, VersionLimitation> {

        public static VersionLimitation createLimitation(final String requiredVersion){
            return new VersionLimitation(requiredVersion) {
                @Override
                public LicensingException createException() {
                    return new LicensingException(String.format("'%s' version of JMX adapter expected.", requiredValue));
                }
            };
        }

        @Override
        public VersionLimitation unmarshal(final String requiredVersion) {
            return createLimitation(requiredVersion);
        }
    }

    @XmlJavaTypeAdapter(PluginVersionLimitationAdapter.class)
    @XmlElement(type = String.class)
    private VersionLimitation maxVersion;

    @XmlJavaTypeAdapter(NotificationFeatureLimitationAdapter.class)
    @XmlElement(type = Boolean.class)
    private ExactLimitation<Boolean> notificationsEnabled;

    /**
     * Verifies version of the JMX-specific version.
     *
     * @param serviceContract Type of the service contract to verify.
     * @throws com.itworks.snamp.licensing.LicensingException Actual version of the service doesn't met to license requirements.
     */
    @Override
    public void verifyServiceVersion(final Class<? extends JmxResourceAdapter> serviceContract) throws LicensingException {
        verifyServiceVersion(maxVersion, serviceContract);
    }

    final void verifyJmxNotificationsFeature(){
        verify(notificationsEnabled, Boolean.TRUE);
    }
}
