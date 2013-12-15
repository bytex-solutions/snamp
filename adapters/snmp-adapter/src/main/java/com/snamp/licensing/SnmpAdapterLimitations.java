package com.snamp.licensing;

import com.snamp.Activator;
import com.snamp.adapters.SnmpAdapterBase;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Roman Sakno
 */
@XmlRootElement(name = "snmpAdapterLimitations")
public final class SnmpAdapterLimitations extends AbstractLicenseLimitations implements PluginLicenseLimitations<SnmpAdapterBase> {

    private static final class PluginVersionLimitationAdapter extends RequirementParser<String, String, VersionLimitation> {

        public static final VersionLimitation createLimitation(final String requiredVersion){
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
            return new ExactLimitation<Boolean>(Boolean.TRUE) {
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
            return createLimitation(v != null && v.booleanValue());
        }
    }

    private static final Activator<SnmpAdapterLimitations> fallbackFactory = new Activator<SnmpAdapterLimitations>() {
        @Override
        public SnmpAdapterLimitations newInstance() {
            return new SnmpAdapterLimitations("0.0", false);
        }
    };

    public static SnmpAdapterLimitations current(){
        return current(SnmpAdapterLimitations.class, fallbackFactory);
    }

    public SnmpAdapterLimitations(){

    }

    private SnmpAdapterLimitations(final String versionLimit, final boolean authenticationEnabled){
        maxVersion = PluginVersionLimitationAdapter.createLimitation(versionLimit);
    }

    @XmlJavaTypeAdapter(PluginVersionLimitationAdapter.class)
    @XmlElement(type = String.class)
    private VersionLimitation maxVersion;

    @XmlJavaTypeAdapter(AuthenticationFeatureLimitationAdapter.class)
    @XmlElement(type = Boolean.class)
    private ExactLimitation<Boolean> authenticationEnabled;

    public final void verifyPluginVersion(final Class<? extends SnmpAdapterBase> pluginImpl) throws LicensingException{
        verifyPluginVersion(maxVersion, pluginImpl);
    }

    public final void verifyAuthenticationFeature(){
        verify(authenticationEnabled, Boolean.TRUE);
    }
}
