package com.snamp.adapters;

import com.snamp.configuration.ConfigurationEntityDescriptionProvider;
import com.snamp.configuration.SnmpAdapterConfigurationDescriptor;
import net.xeoh.plugins.base.annotations.Capabilities;
import org.snmp4j.agent.*;

import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;
import static com.snamp.adapters.AbstractAdapter.makeCapabilities;

/**
 * Represents a base class for SNMP adapter.
 * @author Roman Sakno
 */
public abstract class SnmpAdapterBase extends BaseAgent implements Adapter, NotificationPublisher {
    protected static final Logger log = Logger.getLogger("snamp.snmp.log");
    protected static final int defaultPort = 161;
    protected static final String defaultAddress = "127.0.0.1";
    public static final String adapterName = "snmp";

    private ConfigurationEntityDescriptionProvider configDescr;

    /**
     * Gets a logger associated with this adapter.
     *
     * @return A logger associated with this adapter.
     */
    @Override
    public final Logger getLogger() {
        return log;
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
        if(Objects.equals(objectType, Logger.class))
            return objectType.cast(log);
        else if(Objects.equals(objectType, CommandProcessor.class))
            return objectType.cast(agent);
        else if(Objects.equals(objectType, MOServer.class))
            return objectType.cast(server);
        else if(Objects.equals(ConfigurationEntityDescriptionProvider.class, objectType))
            return objectType.cast(getConfigurationDescriptor());
        else return null;
    }

    /**
     * Returns an array of plug-in capabilities.
     * @return An array of plug-in capabilities.
     */
    @Capabilities
    public final String[] capabilities(){
        return makeCapabilities(adapterName);
    }

    protected SnmpAdapterBase(final File bootCounterFile,
                              final File configFile,
                              final CommandProcessor commandProcessor){
        super(bootCounterFile, configFile, commandProcessor);
        configDescr = null;
    }
}
