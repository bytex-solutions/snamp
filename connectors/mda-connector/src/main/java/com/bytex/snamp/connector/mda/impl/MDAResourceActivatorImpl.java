package com.bytex.snamp.connector.mda.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.mda.DataAcceptorFactory;
import com.bytex.snamp.connector.mda.MDAResourceActivator;
import org.osgi.service.http.HttpService;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author Roman Sakno
 * @version 2.0
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

    @SpecialUse
    public MDAResourceActivatorImpl() {
        super(new MonitoringDataAcceptorFactoryImpl(),
                configurationDescriptor(MDAConnectorDescriptionProvider::getInstance),
                new SimpleDependency<>(HttpService.class));
    }
}
