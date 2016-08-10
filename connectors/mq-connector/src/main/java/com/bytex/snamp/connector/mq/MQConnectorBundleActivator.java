package com.bytex.snamp.connector.mq;

import com.bytex.snamp.ResettableIterator;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.mda.DataAcceptorFactory;
import com.bytex.snamp.connector.mda.MDAResourceActivator;
import com.bytex.snamp.connector.mq.jms.JMSDataAcceptorFactory;
import org.osgi.service.jndi.JNDIContextManager;

import java.util.Iterator;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class MQConnectorBundleActivator extends MDAResourceActivator {
    private static final class MQDataAcceptorFactory extends MonitoringDataAcceptorFactory {
        private final DataAcceptorFactory factory = new JMSDataAcceptorFactory();

        /**
         * Returns an iterator over a set of factories of MDA connector.
         *
         * @return an Iterator.
         */
        @Override
        public Iterator<DataAcceptorFactory> iterator() {
            return ResettableIterator.of(factory);
        }
    }

    private static final class MQConnectorConfigurationDescriptorManager extends ConfigurationEntityDescriptionManager<MQResourceConnectorDescriptionProvider>{

        @Override
        protected MQResourceConnectorDescriptionProvider createConfigurationDescriptionProvider(final RequiredService<?>... dependencies) {
            return MQResourceConnectorDescriptionProvider.getInstance();
        }
    }

    @SpecialUse
    public MQConnectorBundleActivator(){
        super(new MQDataAcceptorFactory(),
                new MQConnectorConfigurationDescriptorManager(),
                new SimpleDependency<>(JNDIContextManager.class));
    }
}
