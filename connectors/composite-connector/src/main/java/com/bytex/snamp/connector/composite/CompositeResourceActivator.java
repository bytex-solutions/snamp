package com.bytex.snamp.connector.composite;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.ManagedResourceActivator;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class CompositeResourceActivator extends ManagedResourceActivator<CompositeResourceConnector> {

    @SpecialUse
    public CompositeResourceActivator(){
        super(CompositeResourceActivator::newResourceConnector,
                configurationDescriptor(CompositeResourceConfigurationDescriptor::getInstance));
    }

    private static CompositeResourceConnector newResourceConnector(final String resourceName,
                                                                   final String connectionString,
                                                                   final Map<String, String> parameters,
                                                                   final RequiredService<?>... dependencies) throws Exception{
        final CompositeResourceConfigurationDescriptor parser = CompositeResourceConfigurationDescriptor.getInstance();
        final CompositeResourceConnector result = new CompositeResourceConnector(resourceName, parser.parseThreadPool(parameters));
        result.update(connectionString, parameters);
        return result;
    }
}
