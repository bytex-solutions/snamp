package com.bytex.snamp.connector.zipkin;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;

import java.util.Map;

/**
 * Collects spans compatible with Twitter Zipkin.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
final class ZipkinConnectorActivator extends ManagedResourceActivator<ZipkinConnector> {
    @SpecialUse
    public ZipkinConnectorActivator(){
        super(ZipkinConnectorActivator::createConnector);
    }

    private static ZipkinConnector createConnector(final String resourceName,
                                                   final String connectionString,
                                                   final Map<String, String> connectionParameters,
                                                   final RequiredService<?>... dependencies) throws Exception{
        return new ZipkinConnector(resourceName, connectionParameters);
    }
}
