package com.snamp.adapters;

import com.snamp.AbstractPlatformService;
import net.xeoh.plugins.base.annotations.Capabilities;

import java.util.logging.Logger;

/**
 * Represents an abstract class for building adapters.
 * @author roman
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
