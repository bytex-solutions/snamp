package com.snamp.adapters;

import net.xeoh.plugins.base.annotations.Capabilities;
import org.snmp4j.agent.*;

import java.io.File;
import java.util.Objects;
import java.util.logging.Logger;
import static com.snamp.adapters.AbstractAdapter.makeCapabilities;

/**
 * Represents a base class for SNMP adapter.
 * @author roman
 */
public abstract class SnmpAdapterBase extends BaseAgent implements Adapter {
    protected static final Logger log = Logger.getLogger("snamp.snmp.log");
    protected static final int defaultPort = 161;
    protected static final String defaultAddress = "127.0.0.1";
    public static final String adapterName = "snmp";

    /**
     * Gets a logger associated with the specified platform service.
     *
     * @return
     */
    @Override
    public final Logger getLogger() {
        return log;
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
    }
}
