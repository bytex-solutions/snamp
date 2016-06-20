package com.bytex.snamp.configuration.internal;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ConfigurationParserException extends RuntimeException {
    private static final long serialVersionUID = -7664174080671979734L;

    ConfigurationParserException(final Class<?> parserType){
        super("Parser %s is not provided by ConfigurationManager service");
    }
}
