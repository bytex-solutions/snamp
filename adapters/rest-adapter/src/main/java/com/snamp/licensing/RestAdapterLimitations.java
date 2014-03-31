package com.snamp.licensing;

import org.apache.commons.collections4.Factory;
import com.snamp.adapters.AbstractAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Represents license limitations for REST adapter. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@XmlRootElement(name = "restAdapterLimitations")
public class RestAdapterLimitations extends AbstractLicenseLimitations implements PluginLicenseLimitations<AbstractAdapter> {

    private static final Factory<RestAdapterLimitations> fallbackFactory = new Factory<RestAdapterLimitations>() {
        @Override
        public RestAdapterLimitations create() {
            return new RestAdapterLimitations("0.0");
        }
    };

    public RestAdapterLimitations(){

    }

    private RestAdapterLimitations(final String version){
        maxVersion = PluginVersionLimitationAdapter.createLimitation(version);
    }

    /**
     * Returns the currently loaded limitations.
     * @return The currently loaded limitations.
     */
    public final static RestAdapterLimitations current(){
        return current(RestAdapterLimitations.class, fallbackFactory);
    }

    private static final class PluginVersionLimitationAdapter extends RequirementParser<String, String, VersionLimitation> {

        public static final VersionLimitation createLimitation(final String requiredVersion){
            return new VersionLimitation(requiredVersion) {
                @Override
                public LicensingException createException() {
                    return new LicensingException(String.format("'%s' version of REST adapter expected.", requiredValue));
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

    /**
     * @param pluginImpl
     * @throws LicensingException
     */
    @Override
    public final void verifyPluginVersion(final Class<? extends AbstractAdapter> pluginImpl) throws LicensingException {
        verifyPluginVersion(maxVersion, pluginImpl);
    }
}