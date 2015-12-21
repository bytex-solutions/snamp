package com.bytex.snamp.connectors.mda.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connectors.mda.DataAcceptorFactory;
import com.bytex.snamp.connectors.mda.MDAResourceActivator;
import org.osgi.service.http.HttpService;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MDAResourceActivatorImpl extends MDAResourceActivator {

    private static final class MonitoringDataAcceptorFactoryImpl extends MonitoringDataAcceptorFactory{
        private final ServiceLoader<DataAcceptorFactory> dataAcceptors;

        private MonitoringDataAcceptorFactoryImpl(){
            dataAcceptors = ServiceLoader.load(DataAcceptorFactory.class, getClass().getClassLoader());
        }

        @Override
        public Iterator<DataAcceptorFactory> iterator() {
            return dataAcceptors.iterator();
        }
    }

    private static final class MDAConfigurationEntityDescriptionManager extends ConfigurationEntityDescriptionManager<MDAResourceConfigurationDescriptorProviderImpl>{

        @Override
        protected MDAResourceConfigurationDescriptorProviderImpl createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return new MDAResourceConfigurationDescriptorProviderImpl();
        }
    }

    @SpecialUse
    public MDAResourceActivatorImpl() {
        super(new MonitoringDataAcceptorFactoryImpl(),
                new MDAConfigurationEntityDescriptionManager(),
                new SimpleDependency<>(HttpService.class));
    }
}