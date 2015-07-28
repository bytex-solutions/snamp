package com.bytex.snamp.adapters.groovy.impl;

import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.internal.annotations.SpecialUse;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class GroovyResourceAdapterActivator extends ResourceAdapterActivator<GroovyResourceAdapter> {
    private static final class GroovyResourceAdapterFactory implements ResourceAdapterFactory<GroovyResourceAdapter>{

        @Override
        public GroovyResourceAdapter createAdapter(final String adapterInstance,
                                                   final RequiredService<?>... dependencies) {
            return new GroovyResourceAdapter(adapterInstance);
        }
    }

    private static final class GroovyResourceAdapterConfigurationManager extends ConfigurationEntityDescriptionManager<GroovyResourceAdapterConfigurationProvider>{

        @Override
        protected GroovyResourceAdapterConfigurationProvider createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return new GroovyResourceAdapterConfigurationProvider();
        }
    }

    @SpecialUse
    public GroovyResourceAdapterActivator(){
        super(new GroovyResourceAdapterFactory(), new GroovyResourceAdapterConfigurationManager());
    }
}
