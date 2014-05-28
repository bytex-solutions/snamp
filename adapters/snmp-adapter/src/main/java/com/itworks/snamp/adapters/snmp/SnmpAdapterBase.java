package com.itworks.snamp.adapters.snmp;

import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProvider;
import net.xeoh.plugins.base.annotations.Capabilities;
import org.snmp4j.agent.*;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import static com.itworks.snamp.adapters.AbstractAdapter.makeCapabilities;

/**
 * Represents a base class for SNMP adapter.
 * @author Roman Sakno
 */
public abstract class SnmpAdapterBase extends BaseAgent {
    protected static final int defaultPort = 161;
    protected static final String defaultAddress = "127.0.0.1";
    public static final String adapterName = "snmp";

    private ConfigurationEntityDescriptionProvider configDescr;

    protected static Logger getAdapterLogger(){
        return SnmpHelpers.getLogger();
    }

    /**
     * Gets a logger associated with this adapter.
     *
     * @return A logger associated with this adapter.
     */
    @Override
    public final Logger getLogger() {
        return getAdapterLogger();
    }

    public final ConfigurationEntityDescriptionProvider getConfigurationDescriptor(){
        if(configDescr == null)
            configDescr = new SnmpAdapterConfigurationDescriptor();
        return configDescr;
    }

    /**
     * Retrieves the service instance.
     *
     * @param objectType Type of the requested service.
     * @param <T>         Type of the required service.
     * @return An instance of the requested service; or {@literal null} if service is not available.
     */
    @Override
    public final <T> T queryObject(final Class<T> objectType) {
        if(objectType == null) return null;
        else if(objectType.isInstance(this))
            return objectType.cast(this);
        else if(Objects.equals(objectType, Logger.class))
            return objectType.cast(getLogger());
        else if(Objects.equals(objectType, CommandProcessor.class))
            return objectType.cast(agent);
        else if(Objects.equals(objectType, MOServer.class))
            return objectType.cast(server);
        else if(Objects.equals(ConfigurationEntityDescriptionProvider.class, objectType))
            return objectType.cast(getConfigurationDescriptor());
        else return null;
    }

    protected SnmpAdapterBase(final File bootCounterFile,
                              final File configFile,
                              final CommandProcessor commandProcessor){
        super(bootCounterFile, configFile, commandProcessor);
        configDescr = null;
    }
}
