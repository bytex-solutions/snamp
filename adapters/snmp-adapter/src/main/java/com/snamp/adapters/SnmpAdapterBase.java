package com.snamp.adapters;

import net.xeoh.plugins.base.annotations.Capabilities;
import org.snmp4j.agent.*;

import java.io.File;
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
