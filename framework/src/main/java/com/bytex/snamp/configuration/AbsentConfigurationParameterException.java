package com.bytex.snamp.configuration;

/**
 * Indicating that the required parameter is not defined in the configuration.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public class AbsentConfigurationParameterException extends Exception {
    private static final long serialVersionUID = -6022218858829602198L;

    private final String parameterName;

    public AbsentConfigurationParameterException(final String parameterName){
        super(String.format("Configuration parameter '%s' is not specified", parameterName));
        this.parameterName = parameterName;
    }

    /**
     * Gets name of the absent configuration parameter.
     * @return The name of the missed configuration parameter.
     */
    public final String getParameterName(){
        return parameterName;
    }
}
