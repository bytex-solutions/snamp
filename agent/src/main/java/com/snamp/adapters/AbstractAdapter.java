package com.snamp.adapters;

import net.xeoh.plugins.base.annotations.Capabilities;

import java.util.logging.Logger;

/**
 * Represents an abstract class for building adapters.
 * @author roman
 */
public abstract class AbstractAdapter implements Adapter {
    /**
     * Represents logger associated with this instance of adapter.
     */
    protected final Logger logger;
    private final String adapterName;

    /**
     * Initializes a new instance of the adapter.
     * @param adapterName
     */
    protected AbstractAdapter(final String adapterName){
        this.adapterName = adapterName;
        logger = getLogger(adapterName);
    }

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
