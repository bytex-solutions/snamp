package com.snamp.adapters;

import com.snamp.AbstractPlatformService;
import com.snamp.MethodStub;
import com.snamp.connectors.ManagementConnector;
import com.snamp.connectors.NotificationSupport;
import com.snamp.hosting.AgentConfiguration;
import net.xeoh.plugins.base.annotations.Capabilities;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Represents an abstract class for building adapters.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class AbstractAdapter extends AbstractPlatformService implements Adapter {
    private final String adapterName;

    /**
     * Initializes a new instance of the adapter.
     * @param adapterName
     */
    protected AbstractAdapter(final String adapterName){
        super(getLogger(adapterName));
        this.adapterName = adapterName;
    }

    /**
     * Returns the logging infrastructure associated with the specified adapter.
     * @param adapterName The system name of the adapter.
     * @return The logger that can be used to emitting logs associated with the specified adapter.
     */
    public static final Logger getLogger(final String adapterName){
        return Logger.getLogger(String.format("snamp.adapters.%s.log", adapterName));
    }

    /**
     * Returns an array of plug-in capabilities.
     * @return An array of plug-in capabilities.
     */
    @Capabilities
    public final String[] capabilities(){
        return makeCapabilities(adapterName);
    }

    /**
     * Creates a new array of capabilities for JSPF infrastructure, you should not use this method directly
     * in your code.
     * @param adapterName The name of the adapter.
     * @return An array of plug-in capabilities.
     */
    public static String[] makeCapabilities(final String adapterName){
        return new String[]{
                String.format("adapter:%s", adapterName)
        };
    }
}
