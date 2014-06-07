package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.licensing.AbstractLicenseLimitations;
import com.itworks.snamp.licensing.FrameworkServiceLimitations;
import com.itworks.snamp.licensing.LicenseReader;
import com.itworks.snamp.licensing.LicensingException;
import org.apache.commons.collections4.Factory;
import org.osgi.framework.Version;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import static com.itworks.snamp.core.AbstractBundleActivator.RequiredServiceAccessor;
import static com.itworks.snamp.core.AbstractBundleActivator.SimpleDependency;

/**
 * @author Roman Sakno
 */
@XmlRootElement(name = "snmpAdapterLimitations")
public final class SnmpAdapterLimitations extends AbstractLicenseLimitations implements FrameworkServiceLimitations<SnmpResourceAdapter> {
    public static final RequiredServiceAccessor<LicenseReader> licenseReader = new SimpleDependency<>(LicenseReader.class);

    private static final class PluginVersionLimitationAdapter extends RequirementParser<Version, String, VersionLimitation> {

        public static VersionLimitation createLimitation(final String requiredVersion){
            return new VersionLimitation(requiredVersion) {
                @Override
                public LicensingException createException() {
                    return new LicensingException(String.format("'%s' version of SNMP adapter expected.", requiredValue));
                }
            };
        }

        @Override
        public VersionLimitation unmarshal(final String requiredVersion) {
            return createLimitation(requiredVersion);
        }
    }

    private static final class AuthenticationFeatureLimitationAdapter extends RequirementParser<Boolean, Boolean, ExactLimitation<Boolean>>{

        public static ExactLimitation<Boolean> createLimitation(final boolean authenticationEnabled){
            return new ExactLimitation<Boolean>(authenticationEnabled) {
                @Override
                public LicensingException createException() {
                    return new LicensingException("SNMPv3 is not allowed.");
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

    static final Factory<SnmpAdapterLimitations> fallbackFactory = new Factory<SnmpAdapterLimitations>() {
        @Override
        public SnmpAdapterLimitations create() {
            return new SnmpAdapterLimitations();
        }
    };

    public static SnmpAdapterLimitations current(){
        return AbstractLicenseLimitations.current(SnmpAdapterLimitations.class, licenseReader, fallbackFactory);
    }

    public SnmpAdapterLimitations(){
        this("0.0", false);
    }

    private SnmpAdapterLimitations(final String versionLimit, final boolean authenticationLimit){
        maxVersion = PluginVersionLimitationAdapter.createLimitation(versionLimit);
        authenticationEnabled = AuthenticationFeatureLimitationAdapter.createLimitation(authenticationLimit);
    }

    @XmlJavaTypeAdapter(PluginVersionLimitationAdapter.class)
    @XmlElement(type = String.class)
    private VersionLimitation maxVersion;

    @XmlJavaTypeAdapter(AuthenticationFeatureLimitationAdapter.class)
    @XmlElement(type = Boolean.class)
    private ExactLimitation<Boolean> authenticationEnabled;

    @Override
    public void verifyServiceVersion(final Class<? extends SnmpResourceAdapter> serviceContract) throws LicensingException {
        verifyServiceVersion(maxVersion, serviceContract);
    }

    final void verifyAuthenticationFeature(){
        verify(authenticationEnabled, Boolean.TRUE);
    }
}
