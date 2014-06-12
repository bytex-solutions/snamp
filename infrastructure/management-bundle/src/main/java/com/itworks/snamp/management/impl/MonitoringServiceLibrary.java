package com.itworks.snamp.management.impl;

import com.itworks.snamp.core.AbstractServiceLibrary;
import com.itworks.snamp.management.SnampManager;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

import javax.management.JMException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class MonitoringServiceLibrary extends AbstractServiceLibrary {
    private static final class SnampManagerProvider extends ProvidedService<SnampManager, SnampManagerImpl>{

        public SnampManagerProvider() {
            super(SnampManager.class);
        }

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         */
        @Override
        protected SnampManagerImpl activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) {
            return new SnampManagerImpl();
        }
    }

    private static final class LogReaderServiceDependency extends RequiredServiceAccessor<LogReaderService>{
        private final LogListener listener;

        protected LogReaderServiceDependency(final LogListener listener) {
            super(LogReaderService.class);
            this.listener = listener;
        }

        @Override
        protected boolean match(final ServiceReference<?> reference) {
            return true;
        }

        @Override
        protected void bind(final LogReaderService serviceInstance, final Dictionary<String, ?> properties) {
            super.bind(serviceInstance, properties);
            serviceInstance.addLogListener(listener);
        }

        @Override
        protected void unbind() {
            getService().removeLogListener(listener);
            super.unbind();
        }
    }

    private final SnampManagedBean bean;

    public MonitoringServiceLibrary(){
        super(new SnampManagerProvider());
        bean = new SnampManagedBean();
    }

    /**
     * Starts the service library.
     *
     * @param bundleLevelDependencies A collection of library-level dependencies to be required for this library.
     * @throws Exception Unable to start service library.
     */
    @Override
    protected void start(final Collection<RequiredService<?>> bundleLevelDependencies) throws Exception {
        bundleLevelDependencies.add(new LogReaderServiceDependency(bean));
    }

    /**
     * Activates this service library.
     *
     * @param activationProperties A collection of library activation properties to fill.
     * @param dependencies         A collection of resolved library-level dependencies.
     * @throws javax.management.JMException Unable to activate this library.
     */
    @Override
    protected void activate(final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws JMException {
        ManagementFactory.getPlatformMBeanServer().registerMBean(bean, new ObjectName(SnampCommonsMXBean.BEAN_NAME));

    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     * @throws JMException Unable to deactivate this library.
     */
    @Override
    protected void deactivate(final ActivationPropertyReader activationProperties) throws JMException {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(SnampCommonsMXBean.BEAN_NAME));
    }
}
