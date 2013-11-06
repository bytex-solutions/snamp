package com.snamp.licensing;

import com.snamp.Activator;
import com.snamp.adapters.SnmpAdapterBase;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Roman Sakno
 */
@XmlRootElement(name = "snmpAdapterLimitations")
public final class SnmpAdapterLimitations extends AbstractLicenseLimitations {

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

    private static final Activator<SnmpAdapterLimitations> fallbackFactory = new Activator<SnmpAdapterLimitations>() {
        @Override
        public SnmpAdapterLimitations newInstance() {
            return new SnmpAdapterLimitations("0");
        }
    };

    public static SnmpAdapterLimitations current(){
        return current(SnmpAdapterLimitations.class, fallbackFactory);
    }

    public SnmpAdapterLimitations(){

    }

    private SnmpAdapterLimitations(final String versionLimit){
        maxVersion = PluginVersionLimitationAdapter.createLimitation(versionLimit);
    }

    @XmlJavaTypeAdapter(PluginVersionLimitationAdapter.class)
    @XmlElement(type = String.class)
    private VersionLimitation maxVersion;

    public final void verifyPluginVersion(final Class<? extends SnmpAdapterBase> pluginImpl) throws LicensingException{
        verify(maxVersion, pluginImpl.getPackage().getImplementationVersion());
    }
}
